package com.financial.domain.controller;

import com.financial.BaseIntegrationTest;
import com.financial.domain.domain.EntityStatus;
import com.financial.domain.dto.*;
import com.financial.rules.dto.RuleCreateRequest;
import com.financial.rules.dto.RuleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTypeControllerIntegrationTest extends BaseIntegrationTest {

    private Long productId;
    private Long scenarioId;

    @BeforeEach
    void setupHierarchy() {
        String uniqueSuffix = "_" + System.currentTimeMillis();

        ProductCreateRequest productRequest = ProductCreateRequest.builder()
                .code("TYPE_TEST_PRODUCT" + uniqueSuffix)
                .name("Test Product for Transaction Types")
                .build();

        ResponseEntity<ProductResponse> productResponse = restTemplate.postForEntity(
                "/api/v1/products",
                productRequest,
                ProductResponse.class);

        productId = productResponse.getBody().getId();

        ScenarioCreateRequest scenarioRequest = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("TYPE_TEST_SCENARIO" + uniqueSuffix)
                .name("Test Scenario for Transaction Types")
                .build();

        ResponseEntity<ScenarioResponse> scenarioResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                scenarioRequest,
                ScenarioResponse.class);

        scenarioId = scenarioResponse.getBody().getId();
    }

    @Test
    void createTransactionType_Success() {
        TransactionTypeCreateRequest request = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("TEST_TYPE")
                .name("Test Transaction Type")
                .description("Test Description")
                .build();

        ResponseEntity<TransactionTypeResponse> response = restTemplate.postForEntity(
                "/api/v1/transaction-types",
                request,
                TransactionTypeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("TEST_TYPE");
        assertThat(response.getBody().getScenarioId()).isEqualTo(scenarioId);
        assertThat(response.getBody().getProductId()).isEqualTo(productId);
        assertThat(response.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void createTransactionType_DuplicateCode_ReturnsConflict() {
        TransactionTypeCreateRequest request = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("DUPLICATE_TYPE")
                .name("First Type")
                .build();

        restTemplate.postForEntity("/api/v1/transaction-types", request, TransactionTypeResponse.class);

        TransactionTypeCreateRequest duplicateRequest = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("DUPLICATE_TYPE")
                .name("Second Type")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/transaction-types",
                duplicateRequest,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getTransactionType_Success() {
        TransactionTypeCreateRequest request = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("GET_TYPE")
                .name("Get Test Type")
                .build();

        ResponseEntity<TransactionTypeResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types",
                request,
                TransactionTypeResponse.class);

        Long typeId = createResponse.getBody().getId();

        ResponseEntity<TransactionTypeResponse> response = restTemplate.getForEntity(
                "/api/v1/transaction-types/" + typeId,
                TransactionTypeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo("GET_TYPE");
    }

    @Test
    void listTransactionTypes_FilterByScenario() {
        TransactionTypeCreateRequest request = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("LIST_TYPE")
                .name("List Test Type")
                .build();

        restTemplate.postForEntity("/api/v1/transaction-types", request, TransactionTypeResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/transaction-types?scenarioId=" + scenarioId,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("LIST_TYPE");
    }

    @Test
    void updateTransactionType_Success() {
        TransactionTypeCreateRequest createRequest = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("UPDATE_TYPE")
                .name("Original Name")
                .build();

        ResponseEntity<TransactionTypeResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types",
                createRequest,
                TransactionTypeResponse.class);

        Long typeId = createResponse.getBody().getId();
        Long version = createResponse.getBody().getVersion();

        TransactionTypeUpdateRequest updateRequest = TransactionTypeUpdateRequest.builder()
                .name("Updated Name")
                .description("New Description")
                .version(version)
                .build();

        ResponseEntity<TransactionTypeResponse> response = restTemplate.exchange(
                "/api/v1/transaction-types/" + typeId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                TransactionTypeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteTransactionType_Success() {
        TransactionTypeCreateRequest request = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("DELETE_TYPE")
                .name("Delete Test Type")
                .build();

        ResponseEntity<TransactionTypeResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types",
                request,
                TransactionTypeResponse.class);

        Long typeId = createResponse.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/transaction-types/" + typeId,
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void transactionTypeLifecycle_Success() {
        TransactionTypeCreateRequest request = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("LIFECYCLE_TYPE")
                .name("Lifecycle Test Type")
                .build();

        ResponseEntity<TransactionTypeResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types",
                request,
                TransactionTypeResponse.class);

        Long typeId = createResponse.getBody().getId();

        ResponseEntity<TransactionTypeResponse> activateResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types/" + typeId + "/activate",
                null,
                TransactionTypeResponse.class);
        assertThat(activateResponse.getBody().getStatus()).isEqualTo(EntityStatus.ACTIVE);

        ResponseEntity<TransactionTypeResponse> archiveResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types/" + typeId + "/archive",
                null,
                TransactionTypeResponse.class);
        assertThat(archiveResponse.getBody().getStatus()).isEqualTo(EntityStatus.ARCHIVED);

        ResponseEntity<TransactionTypeResponse> restoreResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types/" + typeId + "/restore",
                null,
                TransactionTypeResponse.class);
        assertThat(restoreResponse.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void ruleAssociations_Success() {
        TransactionTypeCreateRequest typeRequest = TransactionTypeCreateRequest.builder()
                .scenarioId(scenarioId)
                .code("RULE_ASSOC_TYPE")
                .name("Rule Association Test Type")
                .build();

        ResponseEntity<TransactionTypeResponse> typeResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types",
                typeRequest,
                TransactionTypeResponse.class);

        Long typeId = typeResponse.getBody().getId();

        String uniqueSuffix = "_" + System.currentTimeMillis();
        RuleCreateRequest ruleRequest = RuleCreateRequest.builder()
                .code("TEST_RULE" + uniqueSuffix)
                .name("Test Rule")
                .description("Test rule for association")
                .build();

        ResponseEntity<RuleResponse> ruleResponse = restTemplate.postForEntity(
                "/api/v1/accounting-rules",
                ruleRequest,
                RuleResponse.class);

        Long ruleId = ruleResponse.getBody().getId();

        RuleAssociationRequest assocRequest = RuleAssociationRequest.builder()
                .ruleId(ruleId)
                .sequenceNumber(0)
                .build();

        ResponseEntity<RuleAssociationResponse> addResponse = restTemplate.postForEntity(
                "/api/v1/transaction-types/" + typeId + "/rules",
                assocRequest,
                RuleAssociationResponse.class);

        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(addResponse.getBody().getRuleId()).isEqualTo(ruleId);

        ResponseEntity<String> listResponse = restTemplate.getForEntity(
                "/api/v1/transaction-types/" + typeId + "/rules",
                String.class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).contains("TEST_RULE");

        ResponseEntity<Void> removeResponse = restTemplate.exchange(
                "/api/v1/transaction-types/" + typeId + "/rules/" + ruleId,
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(removeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
