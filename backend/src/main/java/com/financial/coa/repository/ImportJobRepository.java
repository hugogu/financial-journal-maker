package com.financial.coa.repository;

import com.financial.coa.domain.ImportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ImportJob entity.
 */
@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    
    /**
     * Find import jobs by status with pagination.
     */
    Page<ImportJob> findByStatus(ImportJob.ImportStatus status, Pageable pageable);
    
    /**
     * Find recent import jobs ordered by creation date.
     */
    Page<ImportJob> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
