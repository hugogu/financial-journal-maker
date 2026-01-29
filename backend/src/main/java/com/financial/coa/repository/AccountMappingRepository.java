package com.financial.coa.repository;

import com.financial.coa.domain.AccountMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for AccountMapping entity.
 * Provides operations for managing COA to Formance Ledger mappings.
 */
@Repository
public interface AccountMappingRepository extends JpaRepository<AccountMapping, Long> {
    
    /**
     * Find mapping by account code.
     */
    Optional<AccountMapping> findByAccountCode(String accountCode);
    
    /**
     * Check if mapping exists for account code.
     */
    boolean existsByAccountCode(String accountCode);
    
    /**
     * Delete mapping by account code.
     */
    void deleteByAccountCode(String accountCode);
    
    /**
     * Find mapping by Formance Ledger account path.
     */
    Optional<AccountMapping> findByFormanceLedgerAccount(String formanceLedgerAccount);
}
