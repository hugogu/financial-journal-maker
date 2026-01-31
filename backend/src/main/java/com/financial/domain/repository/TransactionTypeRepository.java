package com.financial.domain.repository;

import com.financial.domain.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, Long> {

    Optional<TransactionType> findByScenarioIdAndCode(Long scenarioId, String code);

    boolean existsByScenarioIdAndCode(Long scenarioId, String code);

    List<TransactionType> findByScenarioId(Long scenarioId);

    int countByScenarioId(Long scenarioId);

    @Query(value = "SELECT * FROM transaction_types t WHERE " +
           "(:scenarioId IS NULL OR t.scenario_id = :scenarioId) AND " +
           "(:status IS NULL OR t.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR " +
           "LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(*) FROM transaction_types t WHERE " +
           "(:scenarioId IS NULL OR t.scenario_id = :scenarioId) AND " +
           "(:status IS NULL OR t.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR " +
           "LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Page<TransactionType> findByFilters(
            @Param("scenarioId") Long scenarioId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable);
}
