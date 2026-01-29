package com.financial.coa.service;

import com.financial.coa.domain.Account;
import com.financial.coa.domain.AccountReference;
import com.financial.coa.dto.*;
import com.financial.coa.exception.AccountNotFoundException;
import com.financial.coa.exception.AccountReferencedException;
import com.financial.coa.repository.AccountReferenceRepository;
import com.financial.coa.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing chart of accounts operations.
 * Handles CRUD operations, tree navigation, and reference protection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final AccountReferenceRepository accountReferenceRepository;
    private final AccountValidationService validationService;
    
    /**
     * Create a new account.
     */
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        log.info("Creating account with code: {}", request.getCode());
        
        // Validate
        validationService.validateAccountCodeFormat(request.getCode());
        validationService.validateUniqueCode(request.getCode());
        
        Account parent = validationService.validateParentExists(request.getParentCode());
        validationService.validateNoCircularReference(request.getCode(), parent);
        validationService.validateHierarchyDepth(parent);
        
        // Create account
        Account account = Account.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .sharedAcrossScenarios(request.getSharedAcrossScenarios() != null 
                    ? request.getSharedAcrossScenarios() : false)
                .build();
        
        Account saved = accountRepository.save(account);
        log.info("Account created successfully: {}", saved.getCode());
        
        return toAccountResponse(saved);
    }
    
    /**
     * Get account by code.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountByCode(String code) {
        log.debug("Fetching account: {}", code);
        
        Account account = accountRepository.findByCode(code)
                .orElseThrow(() -> new AccountNotFoundException(code));
        
        return toAccountResponse(account);
    }
    
    /**
     * List all accounts with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AccountResponse> listAccounts(Pageable pageable, Boolean shared) {
        log.debug("Listing accounts - page: {}, shared: {}", pageable.getPageNumber(), shared);
        
        Page<Account> accounts = (shared != null) 
            ? accountRepository.findBySharedAcrossScenarios(shared, pageable)
            : accountRepository.findAll(pageable);
        
        return accounts.map(this::toAccountResponse);
    }
    
    /**
     * Get full account tree.
     */
    @Transactional(readOnly = true)
    public List<AccountTreeNode> getAccountTree(String rootCode) {
        log.debug("Building account tree from root: {}", rootCode);
        
        List<Account> accounts = (rootCode != null && !rootCode.isBlank())
            ? accountRepository.findSubtree(rootCode)
            : accountRepository.findAccountTree();
        
        // Build tree structure
        return buildTree(accounts);
    }
    
    /**
     * Update account (name/description/parent only, code is immutable if referenced).
     * Shared accounts have additional restrictions on modifications.
     */
    @Transactional
    public AccountResponse updateAccount(String code, AccountUpdateRequest request) {
        log.info("Updating account: {}", code);
        
        Account account = accountRepository.findByCode(code)
                .orElseThrow(() -> new AccountNotFoundException(code));
        
        // Check if account is referenced (code cannot be changed)
        long refCount = accountReferenceRepository.countByAccountCode(code);
        if (refCount > 0) {
            log.debug("Account {} is referenced {} times", code, refCount);
        }
        
        // Check cross-scenario constraints for shared accounts
        validateSharedAccountModification(account, refCount);
        
        // Update allowed fields
        account.setName(request.getName());
        account.setDescription(request.getDescription());
        
        // Update parent if specified and different
        if (request.getParentCode() != null) {
            Account newParent = validationService.validateParentExists(request.getParentCode());
            Long currentParentId = (account.getParent() != null) ? account.getParent().getId() : null;
            if (newParent != null && !newParent.getId().equals(currentParentId)) {
                validationService.validateNoCircularReference(account.getCode(), newParent);
                account.setParent(newParent);
            }
        } else if (account.getParent() != null) {
            // Clear parent if explicitly set to null
            account.setParent(null);
        }
        
        account.setVersion(request.getVersion());
        Account updated = accountRepository.save(account);
        
        log.info("Account updated successfully: {}", code);
        return toAccountResponse(updated);
    }
    
    /**
     * Delete account (only if not referenced and has no children).
     */
    @Transactional
    public void deleteAccount(String code) {
        log.info("Attempting to delete account: {}", code);
        
        Account account = accountRepository.findByCode(code)
                .orElseThrow(() -> new AccountNotFoundException(code));
        
        // Check if account has children
        boolean hasChildren = accountRepository.hasChildren(account.getId());
        if (hasChildren) {
            throw new IllegalArgumentException(
                String.format("Cannot delete account '%s': has child accounts", code));
        }
        
        // Check if account is referenced
        long refCount = accountReferenceRepository.countByAccountCode(code);
        if (refCount > 0) {
            throw new AccountReferencedException(code, (int) refCount, "delete");
        }
        
        accountRepository.delete(account);
        log.info("Account deleted successfully: {}", code);
    }
    
    /**
     * Get all references for an account.
     */
    @Transactional(readOnly = true)
    public List<AccountReferenceResponse> getAccountReferences(String code) {
        log.debug("Fetching references for account: {}", code);
        
        // Verify account exists
        if (!accountRepository.existsByCode(code)) {
            throw new AccountNotFoundException(code);
        }
        
        List<AccountReference> references = accountReferenceRepository.findByAccountCode(code);
        
        return references.stream()
                .map(this::toReferenceResponse)
                .toList();
    }
    
    /**
     * Create a reference to an account (called by rule/scenario modules).
     */
    @Transactional
    public AccountReferenceResponse createReference(ReferenceCreateRequest request) {
        log.info("Creating reference: {} -> {} ({})", 
            request.getAccountCode(), request.getReferenceSourceId(), request.getReferenceType());
        
        // Verify account exists
        if (!accountRepository.existsByCode(request.getAccountCode())) {
            throw new AccountNotFoundException(request.getAccountCode());
        }
        
        AccountReference reference = AccountReference.builder()
                .accountCode(request.getAccountCode())
                .referenceSourceId(request.getReferenceSourceId())
                .referenceType(request.getReferenceType())
                .referenceDescription(request.getReferenceDescription())
                .build();
        
        AccountReference saved = accountReferenceRepository.save(reference);
        log.info("Reference created successfully: {}", saved.getId());
        
        return toReferenceResponse(saved);
    }
    
    /**
     * Delete a reference (called when rule/scenario is deleted).
     */
    @Transactional
    public void deleteReference(Long referenceId) {
        log.info("Deleting reference: {}", referenceId);
        
        accountReferenceRepository.deleteById(referenceId);
        log.info("Reference deleted successfully: {}", referenceId);
    }
    
    // Helper methods
    
    private AccountResponse toAccountResponse(Account account) {
        boolean hasChildren = accountRepository.hasChildren(account.getId());
        long refCount = accountReferenceRepository.countByAccountCode(account.getCode());
        
        return AccountResponse.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .description(account.getDescription())
                .parentCode(account.getParent() != null ? account.getParent().getCode() : null)
                .hasChildren(hasChildren)
                .isReferenced(refCount > 0)
                .referenceCount((int) refCount)
                .sharedAcrossScenarios(account.getSharedAcrossScenarios())
                .version(account.getVersion())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
    
    private AccountReferenceResponse toReferenceResponse(AccountReference reference) {
        return AccountReferenceResponse.builder()
                .id(reference.getId())
                .accountCode(reference.getAccountCode())
                .referenceSourceId(reference.getReferenceSourceId())
                .referenceType(reference.getReferenceType())
                .referenceDescription(reference.getReferenceDescription())
                .createdAt(reference.getCreatedAt())
                .build();
    }
    
    private List<AccountTreeNode> buildTree(List<Account> accounts) {
        Map<String, AccountTreeNode> nodeMap = new HashMap<>();
        Map<String, String> parentMap = new HashMap<>();
        
        // Create nodes
        for (Account account : accounts) {
            long refCount = accountReferenceRepository.countByAccountCode(account.getCode());
            
            AccountTreeNode node = AccountTreeNode.builder()
                    .code(account.getCode())
                    .name(account.getName())
                    .description(account.getDescription())
                    .isReferenced(refCount > 0)
                    .children(new ArrayList<>())
                    .build();
            
            nodeMap.put(account.getCode(), node);
            
            if (account.getParent() != null) {
                parentMap.put(account.getCode(), account.getParent().getCode());
            }
        }
        
        // Build hierarchy
        List<AccountTreeNode> roots = new ArrayList<>();
        
        for (Map.Entry<String, AccountTreeNode> entry : nodeMap.entrySet()) {
            String code = entry.getKey();
            AccountTreeNode node = entry.getValue();
            
            String parentCode = parentMap.get(code);
            if (parentCode != null && nodeMap.containsKey(parentCode)) {
                nodeMap.get(parentCode).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        
        return roots;
    }
    
    // Cross-scenario validation methods (T050-T052)
    
    /**
     * Validate that modifications to shared accounts follow cross-scenario rules.
     * Shared accounts that are referenced in multiple scenarios cannot have structural changes.
     */
    private void validateSharedAccountModification(Account account, long referenceCount) {
        if (account.getSharedAcrossScenarios() && referenceCount > 1) {
            log.warn("Attempting to modify shared account {} with {} references", 
                    account.getCode(), referenceCount);
            throw new IllegalArgumentException(
                String.format("Cannot modify shared account '%s': used in %d scenario(s). " +
                    "Shared accounts are immutable once referenced in multiple scenarios.",
                    account.getCode(), referenceCount));
        }
    }
    
    /**
     * Mark an account as shared across scenarios.
     */
    @Transactional
    public AccountResponse markAccountAsShared(String code, boolean shared) {
        log.info("Marking account {} as shared: {}", code, shared);
        
        Account account = accountRepository.findByCode(code)
                .orElseThrow(() -> new AccountNotFoundException(code));
        
        account.setSharedAcrossScenarios(shared);
        Account updated = accountRepository.save(account);
        
        return toAccountResponse(updated);
    }
    
    /**
     * Get list of scenarios using an account.
     * Returns reference source IDs grouped by type.
     */
    @Transactional(readOnly = true)
    public List<String> listAccountScenarios(String code) {
        log.debug("Listing scenarios for account: {}", code);
        
        if (!accountRepository.existsByCode(code)) {
            throw new AccountNotFoundException(code);
        }
        
        return accountReferenceRepository.findByAccountCode(code).stream()
                .filter(ref -> ref.getReferenceType() == AccountReference.ReferenceType.SCENARIO)
                .map(ref -> ref.getReferenceSourceId())
                .distinct()
                .toList();
    }
}
