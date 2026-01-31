package com.financial.rules.repository;

import com.financial.rules.domain.TriggerCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerConditionRepository extends JpaRepository<TriggerCondition, Long> {

    List<TriggerCondition> findByRuleId(Long ruleId);

    void deleteByRuleId(Long ruleId);

    boolean existsByRuleId(Long ruleId);

    long countByRuleId(Long ruleId);
}
