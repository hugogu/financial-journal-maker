package com.financial.rules.repository;

import com.financial.rules.domain.EntryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntryTemplateRepository extends JpaRepository<EntryTemplate, Long> {

    Optional<EntryTemplate> findByRuleId(Long ruleId);

    boolean existsByRuleId(Long ruleId);

    void deleteByRuleId(Long ruleId);
}
