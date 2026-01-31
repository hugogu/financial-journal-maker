package com.financial.rules.repository;

import com.financial.rules.domain.AccountingRule;
import com.financial.rules.domain.RuleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingRuleRepository extends JpaRepository<AccountingRule, Long> {

    Optional<AccountingRule> findByCode(String code);

    boolean existsByCode(String code);

    List<AccountingRule> findByStatus(RuleStatus status);

    Page<AccountingRule> findByStatus(RuleStatus status, Pageable pageable);

    List<AccountingRule> findBySharedAcrossScenariosTrue();

    Page<AccountingRule> findBySharedAcrossScenarios(Boolean shared, Pageable pageable);

    @Query(value = "SELECT * FROM accounting_rules r WHERE " +
           "(:status IS NULL OR r.status = CAST(:status AS VARCHAR)) AND " +
           "(:shared IS NULL OR r.shared_across_scenarios = :shared) AND " +
           "(:search IS NULL OR " +
           "LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(*) FROM accounting_rules r WHERE " +
           "(:status IS NULL OR r.status = CAST(:status AS VARCHAR)) AND " +
           "(:shared IS NULL OR r.shared_across_scenarios = :shared) AND " +
           "(:search IS NULL OR " +
           "LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    Page<AccountingRule> findByFilters(
            @Param("status") String status,
            @Param("shared") Boolean shared,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COUNT(r) FROM AccountingRule r WHERE r.status = :status")
    long countByStatus(@Param("status") RuleStatus status);
}
