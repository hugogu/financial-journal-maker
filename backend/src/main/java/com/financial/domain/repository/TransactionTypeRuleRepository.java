package com.financial.domain.repository;

import com.financial.domain.domain.TransactionTypeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionTypeRuleRepository extends JpaRepository<TransactionTypeRule, Long> {

    List<TransactionTypeRule> findByTransactionTypeIdOrderBySequenceNumberAsc(Long transactionTypeId);

    Optional<TransactionTypeRule> findByTransactionTypeIdAndRuleId(Long transactionTypeId, Long ruleId);

    boolean existsByTransactionTypeIdAndRuleId(Long transactionTypeId, Long ruleId);

    void deleteByTransactionTypeId(Long transactionTypeId);

    int countByTransactionTypeId(Long transactionTypeId);

    @Query("SELECT ttr FROM TransactionTypeRule ttr WHERE ttr.rule.id = :ruleId")
    List<TransactionTypeRule> findByRuleId(@Param("ruleId") Long ruleId);

    @Query("SELECT DISTINCT ttr.rule.id FROM TransactionTypeRule ttr " +
           "JOIN ttr.transactionType tt " +
           "JOIN tt.scenario s " +
           "WHERE s.product.id = :productId")
    List<Long> findRuleIdsByProductId(@Param("productId") Long productId);

    @Query("SELECT DISTINCT ttr.rule.id FROM TransactionTypeRule ttr " +
           "JOIN ttr.transactionType tt " +
           "WHERE tt.scenario.id = :scenarioId")
    List<Long> findRuleIdsByScenarioId(@Param("scenarioId") Long scenarioId);
}
