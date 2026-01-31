package com.financial.domain.controller;

import com.financial.BaseIntegrationTest;
import com.financial.domain.domain.EntityStatus;
import com.financial.domain.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ScenarioControllerIntegrationTest extends BaseIntegrationTest {

    private Long productId;

    @BeforeEach
    void setupProduct() {
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
                .code("SCENARIO_TEST_PRODUCT_" + System.currentTimeMillis())
                .name("Test Product for Scenarios")
                .build();

        ResponseEntity<ProductResponse> productResponse = restTemplate.postForEntity(
                "/api/v1/products",
                productRequest,
                ProductResponse.class);

        productId = productResponse.getBody().getId();
    }

    @Test
    void createScenario_Success() {
        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("TEST_SCENARIO")
                .name("Test Scenario")
                .description("Test Description")
                .triggerDescription("Test Trigger")
                .fundFlowPath("Test Fund Flow")
                .build();

        ResponseEntity<ScenarioResponse> response = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("TEST_SCENARIO");
        assertThat(response.getBody().getProductId()).isEqualTo(productId);
        assertThat(response.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void createScenario_DuplicateCode_ReturnsConflict() {
        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("DUPLICATE_SCENARIO")
                .name("First Scenario")
                .build();

        restTemplate.postForEntity("/api/v1/scenarios", request, ScenarioResponse.class);

        ScenarioCreateRequest duplicateRequest = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("DUPLICATE_SCENARIO")
                .name("Second Scenario")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/scenarios",
                duplicateRequest,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createScenario_UnderArchivedProduct_ReturnsBadRequest() {
        restTemplate.postForEntity("/api/v1/products/" + productId + "/archive", null, ProductResponse.class);

        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("ARCHIVED_PARENT")
                .name("Scenario Under Archived Product")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getScenario_Success() {
        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("GET_SCENARIO")
                .name("Get Test Scenario")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();

        ResponseEntity<ScenarioResponse> response = restTemplate.getForEntity(
                "/api/v1/scenarios/" + scenarioId,
                ScenarioResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo("GET_SCENARIO");
    }

    @Test
    void listScenarios_FilterByProduct() {
        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("LIST_SCENARIO")
                .name("List Test Scenario")
                .build();

        restTemplate.postForEntity("/api/v1/scenarios", request, ScenarioResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/scenarios?productId=" + productId,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("LIST_SCENARIO");
    }

    @Test
    void updateScenario_Success() {
        ScenarioCreateRequest createRequest = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("UPDATE_SCENARIO")
                .name("Original Name")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                createRequest,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();
        Long version = createResponse.getBody().getVersion();

        ScenarioUpdateRequest updateRequest = ScenarioUpdateRequest.builder()
                .name("Updated Name")
                .description("New Description")
                .version(version)
                .build();

        ResponseEntity<ScenarioResponse> response = restTemplate.exchange(
                "/api/v1/scenarios/" + scenarioId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                ScenarioResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteScenario_Success() {
        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("DELETE_SCENARIO")
                .name("Delete Test Scenario")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/scenarios/" + scenarioId,
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void scenarioLifecycle_Success() {
        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("LIFECYCLE_SCENARIO")
                .name("Lifecycle Test Scenario")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();

        ResponseEntity<ScenarioResponse> activateResponse = restTemplate.postForEntity(
                "/api/v1/scenarios/" + scenarioId + "/activate",
                null,
                ScenarioResponse.class);
        assertThat(activateResponse.getBody().getStatus()).isEqualTo(EntityStatus.ACTIVE);

        ResponseEntity<ScenarioResponse> archiveResponse = restTemplate.postForEntity(
                "/api/v1/scenarios/" + scenarioId + "/archive",
                null,
                ScenarioResponse.class);
        assertThat(archiveResponse.getBody().getStatus()).isEqualTo(EntityStatus.ARCHIVED);

        ResponseEntity<ScenarioResponse> restoreResponse = restTemplate.postForEntity(
                "/api/v1/scenarios/" + scenarioId + "/restore",
                null,
                ScenarioResponse.class);
        assertThat(restoreResponse.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void getScenarioRules_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
                .code("RULES_PROD" + uniqueSuffix)
                .name("Rules Product")
                .build();

        ResponseEntity<ProductResponse> productResponse = restTemplate.postForEntity(
                "/api/v1/products",
                productRequest,
                ProductResponse.class);

        Long productId = productResponse.getBody().getId();

        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("RULES_SCENARIO" + uniqueSuffix)
                .name("Rules Test Scenario")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/scenarios/" + scenarioId + "/rules",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getScenarioAccounts_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
                .code("ACCOUNTS_PROD" + uniqueSuffix)
                .name("Accounts Product")
                .build();

        ResponseEntity<ProductResponse> productResponse = restTemplate.postForEntity(
                "/api/v1/products",
                productRequest,
                ProductResponse.class);

        Long productId = productResponse.getBody().getId();

        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("ACCOUNTS_SCENARIO" + uniqueSuffix)
                .name("Accounts Test Scenario")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/scenarios/" + scenarioId + "/accounts",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void cloneScenario_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
                .code("CLONE_PROD" + uniqueSuffix)
                .name("Clone Product")
                .build();

        ResponseEntity<ProductResponse> productResponse = restTemplate.postForEntity(
                "/api/v1/products",
                productRequest,
                ProductResponse.class);

        Long productId = productResponse.getBody().getId();

        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(productId)
                .code("CLONE_SOURCE" + uniqueSuffix)
                .name("Clone Source Scenario")
                .description("Original Description")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();

        ScenarioCloneRequest cloneRequest = ScenarioCloneRequest.builder()
                .newCode("CLONE_TARGET" + uniqueSuffix)
                .newName("Cloned Scenario")
                .build();

        ResponseEntity<ScenarioResponse> cloneResponse = restTemplate.postForEntity(
                "/api/v1/scenarios/" + scenarioId + "/clone",
                cloneRequest,
                ScenarioResponse.class);

        assertThat(cloneResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cloneResponse.getBody()).isNotNull();
        assertThat(cloneResponse.getBody().getCode()).isEqualTo("CLONE_TARGET" + uniqueSuffix);
        assertThat(cloneResponse.getBody().getName()).isEqualTo("Cloned Scenario");
        assertThat(cloneResponse.getBody().getDescription()).isEqualTo("Original Description");
        assertThat(cloneResponse.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void cloneScenario_ToDifferentProduct() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        
        ProductCreateRequest sourceProductRequest = ProductCreateRequest.builder()
                .code("SRC_PROD" + uniqueSuffix)
                .name("Source Product")
                .build();
        ResponseEntity<ProductResponse> sourceProductResponse = restTemplate.postForEntity(
                "/api/v1/products",
                sourceProductRequest,
                ProductResponse.class);
        Long sourceProductId = sourceProductResponse.getBody().getId();

        ProductCreateRequest targetProductRequest = ProductCreateRequest.builder()
                .code("TGT_PROD" + uniqueSuffix)
                .name("Target Product")
                .build();
        ResponseEntity<ProductResponse> targetProductResponse = restTemplate.postForEntity(
                "/api/v1/products",
                targetProductRequest,
                ProductResponse.class);
        Long targetProductId = targetProductResponse.getBody().getId();

        ScenarioCreateRequest request = ScenarioCreateRequest.builder()
                .productId(sourceProductId)
                .code("MOVE_SCENARIO" + uniqueSuffix)
                .name("Move Scenario")
                .build();

        ResponseEntity<ScenarioResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/scenarios",
                request,
                ScenarioResponse.class);

        Long scenarioId = createResponse.getBody().getId();

        ScenarioCloneRequest cloneRequest = ScenarioCloneRequest.builder()
                .targetProductId(targetProductId)
                .newCode("MOVED_SCENARIO" + uniqueSuffix)
                .build();

        ResponseEntity<ScenarioResponse> cloneResponse = restTemplate.postForEntity(
                "/api/v1/scenarios/" + scenarioId + "/clone",
                cloneRequest,
                ScenarioResponse.class);

        assertThat(cloneResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cloneResponse.getBody()).isNotNull();
        assertThat(cloneResponse.getBody().getProductId()).isEqualTo(targetProductId);
    }
}
