package com.financial.coa.repository;

import com.financial.coa.domain.AccountReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for AccountReference entity.
 * Tracks references to accounts from rules and scenarios.
 */
@Repository
public interface AccountReferenceRepository extends JpaRepository<AccountReference, Long> {
    
    /**
     * Check if any references exist for a given account code.
     */
    boolean existsByAccountCode(String accountCode);
    
    /**
     * Find all references for a given account code.
     */
    List<AccountReference> findByAccountCode(String accountCode);
    
    /**
     * Count references for a given account code.
     */
    long countByAccountCode(String accountCode);
    
    /**
     * Find all references from a specific source (rule or scenario).
     */
    List<AccountReference> findByReferenceSourceId(String referenceSourceId);
    
    /**
     * Delete all references from a specific source.
     * Used when a rule or scenario is deleted.
     */
    void deleteByReferenceSourceId(String referenceSourceId);
}
