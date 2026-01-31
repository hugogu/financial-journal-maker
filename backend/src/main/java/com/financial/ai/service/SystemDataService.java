package com.financial.ai.service;

import com.financial.coa.domain.Account;
import com.financial.coa.repository.AccountRepository;
import com.financial.domain.domain.Product;
import com.financial.domain.domain.Scenario;
import com.financial.domain.domain.TransactionType;
import com.financial.domain.repository.ProductRepository;
import com.financial.domain.repository.ScenarioRepository;
import com.financial.domain.repository.TransactionTypeRepository;
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
public class SystemDataService {

    private final ProductRepository productRepository;
    private final ScenarioRepository scenarioRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final AccountingRuleRepository accountingRuleRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Scenario> getAllScenarios() {
        return scenarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Scenario> getScenariosByProduct(Long productId) {
        return scenarioRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<TransactionType> getAllTransactionTypes() {
        return transactionTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TransactionType> getTransactionTypesByScenario(Long scenarioId) {
        return transactionTypeRepository.findByScenarioId(scenarioId);
    }

    @Transactional(readOnly = true)
    public List<AccountingRule> getAllAccountingRules() {
        return accountingRuleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Account> getChartOfAccounts() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public String getProductsSummary() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return "No products defined yet.";
        }

        return products.stream()
                .map(p -> String.format("- %s (%s): %s [%s]",
                        p.getName(),
                        p.getCode(),
                        p.getDescription() != null ? p.getDescription() : "No description",
                        p.getStatus()))
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public String getScenariosSummary() {
        List<Scenario> scenarios = scenarioRepository.findAll();
        if (scenarios.isEmpty()) {
            return "No scenarios defined yet.";
        }

        return scenarios.stream()
                .map(s -> String.format("- %s (%s) [Product: %s]: %s [%s]",
                        s.getName(),
                        s.getCode(),
                        s.getProduct() != null ? s.getProduct().getCode() : "N/A",
                        s.getDescription() != null ? s.getDescription() : "No description",
                        s.getStatus()))
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public String getTransactionTypesSummary() {
        List<TransactionType> types = transactionTypeRepository.findAll();
        if (types.isEmpty()) {
            return "No transaction types defined yet.";
        }

        return types.stream()
                .map(t -> String.format("- %s (%s) [Scenario: %s]: %s [%s]",
                        t.getName(),
                        t.getCode(),
                        t.getScenario() != null ? t.getScenario().getCode() : "N/A",
                        t.getDescription() != null ? t.getDescription() : "No description",
                        t.getStatus()))
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public String getAccountingRulesSummary() {
        List<AccountingRule> rules = accountingRuleRepository.findAll();
        if (rules.isEmpty()) {
            return "No accounting rules defined yet.";
        }

        return rules.stream()
                .map(r -> String.format("- %s (%s): %s [%s]",
                        r.getName(),
                        r.getCode(),
                        r.getDescription() != null ? r.getDescription() : "No description",
                        r.getStatus()))
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public String getChartOfAccountsSummary() {
        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            return "No accounts defined yet.";
        }

        return accounts.stream()
                .limit(50) // Limit to avoid huge prompts
                .map(a -> String.format("- %s: %s%s",
                        a.getCode(),
                        a.getName(),
                        a.getSharedAcrossScenarios() ? " (shared)" : ""))
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public SystemDataContext buildContextForPhase(String phase) {
        log.debug("Building system data context for phase: {}", phase);

        SystemDataContext context = new SystemDataContext();

        switch (phase.toUpperCase()) {
            case "PRODUCT":
                context.setExistingProducts(getProductsSummary());
                break;
            case "SCENARIO":
                context.setExistingProducts(getProductsSummary());
                context.setExistingScenarios(getScenariosSummary());
                break;
            case "TRANSACTION_TYPE":
                context.setExistingProducts(getProductsSummary());
                context.setExistingScenarios(getScenariosSummary());
                context.setExistingTransactionTypes(getTransactionTypesSummary());
                break;
            case "ACCOUNTING":
                context.setExistingProducts(getProductsSummary());
                context.setExistingScenarios(getScenariosSummary());
                context.setExistingTransactionTypes(getTransactionTypesSummary());
                context.setAccountingRules(getAccountingRulesSummary());
                context.setChartOfAccounts(getChartOfAccountsSummary());
                break;
            default:
                // Include all for unknown phases
                context.setExistingProducts(getProductsSummary());
                context.setExistingScenarios(getScenariosSummary());
                context.setExistingTransactionTypes(getTransactionTypesSummary());
                context.setAccountingRules(getAccountingRulesSummary());
                context.setChartOfAccounts(getChartOfAccountsSummary());
        }

        return context;
    }

    @lombok.Data
    public static class SystemDataContext {
        private String existingProducts = "";
        private String existingScenarios = "";
        private String existingTransactionTypes = "";
        private String accountingRules = "";
        private String chartOfAccounts = "";
    }
}
