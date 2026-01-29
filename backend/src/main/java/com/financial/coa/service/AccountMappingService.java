package com.financial.coa.service;

import com.financial.coa.domain.AccountMapping;
import com.financial.coa.dto.MappingCreateRequest;
import com.financial.coa.dto.MappingResponse;
import com.financial.coa.dto.MappingUpdateRequest;
import com.financial.coa.exception.AccountNotFoundException;
import com.financial.coa.repository.AccountMappingRepository;
import com.financial.coa.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing account to Formance Ledger mappings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountMappingService {
    
    private final AccountMappingRepository mappingRepository;
    private final AccountRepository accountRepository;
    
    /**
     * Create a new mapping between COA account and Formance Ledger account.
     */
    @Transactional
    public MappingResponse createMapping(MappingCreateRequest request) {
        log.info("Creating mapping: {} -> {}", request.getAccountCode(), request.getFormanceLedgerAccount());
        
        // Validate account exists
        if (!accountRepository.existsByCode(request.getAccountCode())) {
            throw new AccountNotFoundException(request.getAccountCode());
        }
        
        // Check if mapping already exists
        if (mappingRepository.existsByAccountCode(request.getAccountCode())) {
            throw new IllegalArgumentException(
                String.format("Mapping already exists for account: %s", request.getAccountCode()));
        }
        
        AccountMapping mapping = AccountMapping.builder()
                .accountCode(request.getAccountCode())
                .formanceLedgerAccount(request.getFormanceLedgerAccount())
                .build();
        
        AccountMapping saved = mappingRepository.save(mapping);
        log.info("Mapping created successfully: {} -> {}", saved.getAccountCode(), saved.getFormanceLedgerAccount());
        
        return toMappingResponse(saved);
    }
    
    /**
     * Get mapping by account code.
     */
    @Transactional(readOnly = true)
    public MappingResponse getMapping(String accountCode) {
        log.debug("Fetching mapping for account: {}", accountCode);
        
        AccountMapping mapping = mappingRepository.findByAccountCode(accountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("No mapping found for account: %s", accountCode)));
        
        return toMappingResponse(mapping);
    }
    
    /**
     * List all mappings with pagination.
     */
    @Transactional(readOnly = true)
    public Page<MappingResponse> listMappings(Pageable pageable) {
        log.debug("Listing mappings - page: {}", pageable.getPageNumber());
        
        return mappingRepository.findAll(pageable).map(this::toMappingResponse);
    }
    
    /**
     * Update existing mapping.
     */
    @Transactional
    public MappingResponse updateMapping(String accountCode, MappingUpdateRequest request) {
        log.info("Updating mapping for account: {}", accountCode);
        
        AccountMapping mapping = mappingRepository.findByAccountCode(accountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("No mapping found for account: %s", accountCode)));
        
        mapping.setFormanceLedgerAccount(request.getFormanceLedgerAccount());
        mapping.setVersion(request.getVersion());
        
        AccountMapping updated = mappingRepository.save(mapping);
        log.info("Mapping updated successfully: {} -> {}", updated.getAccountCode(), updated.getFormanceLedgerAccount());
        
        return toMappingResponse(updated);
    }
    
    /**
     * Delete mapping by account code.
     */
    @Transactional
    public void deleteMapping(String accountCode) {
        log.info("Deleting mapping for account: {}", accountCode);
        
        if (!mappingRepository.existsByAccountCode(accountCode)) {
            throw new IllegalArgumentException(
                String.format("No mapping found for account: %s", accountCode));
        }
        
        mappingRepository.deleteByAccountCode(accountCode);
        log.info("Mapping deleted successfully for account: {}", accountCode);
    }
    
    private MappingResponse toMappingResponse(AccountMapping mapping) {
        return MappingResponse.builder()
                .id(mapping.getId())
                .accountCode(mapping.getAccountCode())
                .formanceLedgerAccount(mapping.getFormanceLedgerAccount())
                .version(mapping.getVersion())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }
}
