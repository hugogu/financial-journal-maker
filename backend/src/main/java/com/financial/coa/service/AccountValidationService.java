package com.financial.coa.service;

import com.financial.coa.domain.Account;
import com.financial.coa.exception.CircularReferenceException;
import com.financial.coa.exception.DuplicateAccountCodeException;
import com.financial.coa.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service for validating account operations.
 * Handles validation logic for account codes, parent relationships, and circular references.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountValidationService {
    
    private final AccountRepository accountRepository;
    
    private static final Pattern ACCOUNT_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9.-]+$");
    private static final int MAX_HIERARCHY_DEPTH = 10;
    
    /**
     * Validate that account code is unique.
     */
    public void validateUniqueCode(String code) {
        if (accountRepository.existsByCode(code)) {
            log.warn("Duplicate account code attempted: {}", code);
            throw new DuplicateAccountCodeException(code);
        }
    }
    
    /**
     * Validate that account code format is correct.
     */
    public void validateAccountCodeFormat(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Account code cannot be null or empty");
        }
        
        if (code.length() > 50) {
            throw new IllegalArgumentException("Account code must not exceed 50 characters");
        }
        
        if (!ACCOUNT_CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                "Account code can only contain alphanumeric characters, dots, and hyphens");
        }
    }
    
    /**
     * Validate that parent account exists if specified.
     */
    public Account validateParentExists(String parentCode) {
        if (parentCode == null || parentCode.isBlank()) {
            return null;
        }
        
        return accountRepository.findByCode(parentCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Parent account not found: %s", parentCode)));
    }
    
    /**
     * Validate that creating/updating account with given parent won't create circular reference.
     * Uses depth-first search to detect cycles.
     */
    public void validateNoCircularReference(String accountCode, Account parent) {
        if (parent == null) {
            return; // No parent means no circular reference possible
        }
        
        Set<String> visited = new HashSet<>();
        visited.add(accountCode);
        
        Account current = parent;
        int depth = 0;
        
        while (current != null) {
            depth++;
            
            // Check for circular reference
            if (visited.contains(current.getCode())) {
                log.warn("Circular reference detected: {} -> {}", accountCode, current.getCode());
                throw new CircularReferenceException(accountCode, current.getCode());
            }
            
            // Check maximum depth
            if (depth > MAX_HIERARCHY_DEPTH) {
                log.warn("Maximum hierarchy depth exceeded for account: {}", accountCode);
                throw new IllegalArgumentException(
                    String.format("Maximum hierarchy depth (%d) exceeded", MAX_HIERARCHY_DEPTH));
            }
            
            visited.add(current.getCode());
            current = current.getParent();
        }
    }
    
    /**
     * Validate hierarchy depth doesn't exceed maximum.
     */
    public void validateHierarchyDepth(Account parent) {
        if (parent == null) {
            return;
        }
        
        int depth = 0;
        Account current = parent;
        
        while (current != null) {
            depth++;
            if (depth >= MAX_HIERARCHY_DEPTH) {
                throw new IllegalArgumentException(
                    String.format("Cannot create account: parent hierarchy depth exceeds maximum (%d levels)",
                        MAX_HIERARCHY_DEPTH));
            }
            current = current.getParent();
        }
    }
}
