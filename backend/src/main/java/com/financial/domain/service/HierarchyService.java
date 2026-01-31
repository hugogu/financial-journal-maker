package com.financial.domain.service;

import com.financial.domain.domain.Product;
import com.financial.domain.domain.Scenario;
import com.financial.domain.domain.TransactionType;
import com.financial.domain.dto.*;
import com.financial.domain.exception.EntityNotFoundException;
import com.financial.domain.repository.ProductRepository;
import com.financial.domain.repository.ScenarioRepository;
import com.financial.domain.repository.TransactionTypeRepository;
import com.financial.domain.repository.TransactionTypeRuleRepository;
import com.financial.rules.domain.AccountingRule;
import com.financial.rules.repository.AccountingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyService {

    private final ProductRepository productRepository;
    private final ScenarioRepository scenarioRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionTypeRuleRepository transactionTypeRuleRepository;
    private final AccountingRuleRepository accountingRuleRepository;

    @Transactional(readOnly = true)
    public ProductTreeResponse getProductTree(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));

        List<Scenario> scenarios = scenarioRepository.findByProductId(productId);
        
        List<ScenarioTreeNode> scenarioNodes = scenarios.stream()
                .map(scenario -> {
                    List<TransactionType> types = transactionTypeRepository.findByScenarioId(scenario.getId());
                    List<TransactionTypeTreeNode> typeNodes = types.stream()
                            .map(type -> {
                                int ruleCount = transactionTypeRuleRepository.countByTransactionTypeId(type.getId());
                                return TransactionTypeTreeNode.fromEntity(type, ruleCount);
                            })
                            .collect(Collectors.toList());
                    return ScenarioTreeNode.fromEntity(scenario, typeNodes);
                })
                .collect(Collectors.toList());

        return ProductTreeResponse.fromEntity(product, scenarioNodes);
    }

    @Transactional(readOnly = true)
    public List<RuleSummary> getProductRules(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));

        List<Long> ruleIds = transactionTypeRuleRepository.findRuleIdsByProductId(productId);
        
        return ruleIds.stream()
                .map(ruleId -> accountingRuleRepository.findById(ruleId).orElse(null))
                .filter(rule -> rule != null)
                .map(RuleSummary::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RuleSummary> getScenarioRules(Long scenarioId) {
        scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new EntityNotFoundException("Scenario", scenarioId));

        List<Long> ruleIds = transactionTypeRuleRepository.findRuleIdsByScenarioId(scenarioId);
        
        return ruleIds.stream()
                .map(ruleId -> accountingRuleRepository.findById(ruleId).orElse(null))
                .filter(rule -> rule != null)
                .map(RuleSummary::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountSummary> getProductAccounts(Long productId) {
        List<RuleSummary> rules = getProductRules(productId);
        return extractAccountsFromRules(rules);
    }

    @Transactional(readOnly = true)
    public List<AccountSummary> getScenarioAccounts(Long scenarioId) {
        List<RuleSummary> rules = getScenarioRules(scenarioId);
        return extractAccountsFromRules(rules);
    }

    private List<AccountSummary> extractAccountsFromRules(List<RuleSummary> rules) {
        List<AccountSummary> allAccounts = new java.util.ArrayList<>();
        
        for (RuleSummary ruleSummary : rules) {
            AccountingRule fullRule = accountingRuleRepository.findById(ruleSummary.getId()).orElse(null);
            if (fullRule == null || fullRule.getEntryTemplate() == null) {
                continue;
            }
            for (var line : fullRule.getEntryTemplate().getLines()) {
                allAccounts.add(AccountSummary.builder()
                        .accountCode(line.getAccountCode())
                        .usedInRules(ruleSummary.getCode())
                        .build());
            }
        }
        
        return allAccounts.stream()
                .collect(Collectors.groupingBy(AccountSummary::getAccountCode))
                .entrySet().stream()
                .map(entry -> AccountSummary.builder()
                        .accountCode(entry.getKey())
                        .usedInRules(entry.getValue().stream()
                                .map(AccountSummary::getUsedInRules)
                                .distinct()
                                .collect(Collectors.joining(", ")))
                        .build())
                .collect(Collectors.toList());
    }
}
