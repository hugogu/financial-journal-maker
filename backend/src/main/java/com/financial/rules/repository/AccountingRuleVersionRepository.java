package com.financial.rules.repository;

import com.financial.rules.domain.AccountingRuleVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingRuleVersionRepository extends JpaRepository<AccountingRuleVersion, Long> {

    List<AccountingRuleVersion> findByRuleIdOrderByVersionNumberDesc(Long ruleId);

    Page<AccountingRuleVersion> findByRuleIdOrderByVersionNumberDesc(Long ruleId, Pageable pageable);

    Optional<AccountingRuleVersion> findByRuleIdAndVersionNumber(Long ruleId, Integer versionNumber);

    Optional<AccountingRuleVersion> findTopByRuleIdOrderByVersionNumberDesc(Long ruleId);

    boolean existsByRuleIdAndVersionNumber(Long ruleId, Integer versionNumber);

    void deleteByRuleId(Long ruleId);
}
