package com.financial.ai.controller;

import com.financial.ai.service.SystemDataService;
import com.financial.coa.CoaApplication;
import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Product;
import com.financial.domain.domain.Scenario;
import com.financial.domain.domain.TransactionType;
import com.financial.domain.repository.ProductRepository;
import com.financial.domain.repository.ScenarioRepository;
import com.financial.domain.repository.TransactionTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CoaApplication.class)
@ActiveProfiles("test")
@Transactional
class SystemDataIntegrationTest {

    @Autowired
    private SystemDataService systemDataService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ScenarioRepository scenarioRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @BeforeEach
    void setUp() {
        transactionTypeRepository.deleteAll();
        scenarioRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void getProductsSummary_WithProducts_ReturnsSummary() {
        Product product = Product.builder()
                .code("LOAN")
                .name("Consumer Loans")
                .description("Consumer lending products")
                .status(EntityStatus.ACTIVE)
                .build();
        productRepository.save(product);

        String summary = systemDataService.getProductsSummary();

        assertThat(summary).contains("Consumer Loans");
        assertThat(summary).contains("LOAN");
        assertThat(summary).contains("ACTIVE");
    }

    @Test
    void getProductsSummary_Empty_ReturnsMessage() {
        String summary = systemDataService.getProductsSummary();
        assertThat(summary).contains("No products defined yet");
    }

    @Test
    void getScenariosSummary_WithScenarios_ReturnsSummary() {
        Product product = Product.builder()
                .code("LOAN")
                .name("Consumer Loans")
                .status(EntityStatus.ACTIVE)
                .build();
        productRepository.save(product);

        Scenario scenario = Scenario.builder()
                .code("DISB")
                .name("Loan Disbursement")
                .description("Disbursement of loan funds")
                .product(product)
                .status(EntityStatus.ACTIVE)
                .build();
        scenarioRepository.save(scenario);

        String summary = systemDataService.getScenariosSummary();

        assertThat(summary).contains("Loan Disbursement");
        assertThat(summary).contains("DISB");
        assertThat(summary).contains("LOAN");
    }

    @Test
    void getTransactionTypesSummary_WithTypes_ReturnsSummary() {
        Product product = Product.builder()
                .code("LOAN")
                .name("Consumer Loans")
                .status(EntityStatus.ACTIVE)
                .build();
        productRepository.save(product);

        Scenario scenario = Scenario.builder()
                .code("DISB")
                .name("Loan Disbursement")
                .product(product)
                .status(EntityStatus.ACTIVE)
                .build();
        scenarioRepository.save(scenario);

        TransactionType type = TransactionType.builder()
                .code("PRINCIPAL")
                .name("Principal Payment")
                .description("Principal disbursement")
                .scenario(scenario)
                .status(EntityStatus.ACTIVE)
                .build();
        transactionTypeRepository.save(type);

        String summary = systemDataService.getTransactionTypesSummary();

        assertThat(summary).contains("Principal Payment");
        assertThat(summary).contains("PRINCIPAL");
        assertThat(summary).contains("DISB");
    }

    @Test
    void buildContextForPhase_Product_IncludesProducts() {
        Product product = Product.builder()
                .code("DEPOSIT")
                .name("Savings Deposit")
                .status(EntityStatus.ACTIVE)
                .build();
        productRepository.save(product);

        SystemDataService.SystemDataContext context = 
                systemDataService.buildContextForPhase("PRODUCT");

        assertThat(context.getExistingProducts()).contains("Savings Deposit");
        assertThat(context.getExistingScenarios()).isEmpty();
    }

    @Test
    void buildContextForPhase_Scenario_IncludesProductsAndScenarios() {
        Product product = Product.builder()
                .code("DEPOSIT")
                .name("Savings Deposit")
                .status(EntityStatus.ACTIVE)
                .build();
        productRepository.save(product);

        Scenario scenario = Scenario.builder()
                .code("INTEREST")
                .name("Interest Accrual")
                .product(product)
                .status(EntityStatus.ACTIVE)
                .build();
        scenarioRepository.save(scenario);

        SystemDataService.SystemDataContext context = 
                systemDataService.buildContextForPhase("SCENARIO");

        assertThat(context.getExistingProducts()).contains("Savings Deposit");
        assertThat(context.getExistingScenarios()).contains("Interest Accrual");
    }

    @Test
    void buildContextForPhase_Accounting_IncludesAllData() {
        Product product = Product.builder()
                .code("PAYMENT")
                .name("Payment Services")
                .status(EntityStatus.ACTIVE)
                .build();
        productRepository.save(product);

        Scenario scenario = Scenario.builder()
                .code("TRANSFER")
                .name("Fund Transfer")
                .product(product)
                .status(EntityStatus.ACTIVE)
                .build();
        scenarioRepository.save(scenario);

        TransactionType type = TransactionType.builder()
                .code("DEBIT")
                .name("Debit Transfer")
                .scenario(scenario)
                .status(EntityStatus.ACTIVE)
                .build();
        transactionTypeRepository.save(type);

        SystemDataService.SystemDataContext context = 
                systemDataService.buildContextForPhase("ACCOUNTING");

        assertThat(context.getExistingProducts()).contains("Payment Services");
        assertThat(context.getExistingScenarios()).contains("Fund Transfer");
        assertThat(context.getExistingTransactionTypes()).contains("Debit Transfer");
    }
}
