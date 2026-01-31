package com.financial.domain.controller;

import com.financial.BaseIntegrationTest;
import com.financial.domain.domain.EntityStatus;
import com.financial.domain.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("TEST_PRODUCT")
                .name("Test Product")
                .description("Test Description")
                .businessModel("Test Business Model")
                .participants("Test Participants")
                .fundFlow("Test Fund Flow")
                .build();

        ResponseEntity<ProductResponse> response = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("TEST_PRODUCT");
        assertThat(response.getBody().getName()).isEqualTo("Test Product");
        assertThat(response.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    void createProduct_DuplicateCode_ReturnsConflict() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("DUPLICATE_CODE")
                .name("First Product")
                .build();

        restTemplate.postForEntity("/api/v1/products", request, ProductResponse.class);

        ProductCreateRequest duplicateRequest = ProductCreateRequest.builder()
                .code("DUPLICATE_CODE")
                .name("Second Product")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/products",
                duplicateRequest,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("GET_TEST")
                .name("Get Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/v1/products/" + productId,
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("GET_TEST");
    }

    @Test
    void getProduct_NotFound_Returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/products/99999",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProductByCode_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("CODE_LOOKUP")
                .name("Code Lookup Product")
                .build();

        restTemplate.postForEntity("/api/v1/products", request, ProductResponse.class);

        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/v1/products/code/CODE_LOOKUP",
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Code Lookup Product");
    }

    @Test
    void listProducts_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest request1 = ProductCreateRequest.builder()
                .code("LIST_TEST_1" + uniqueSuffix)
                .name("List Test 1")
                .build();

        ProductCreateRequest request2 = ProductCreateRequest.builder()
                .code("LIST_TEST_2" + uniqueSuffix)
                .name("List Test 2")
                .build();

        restTemplate.postForEntity("/api/v1/products", request1, ProductResponse.class);
        restTemplate.postForEntity("/api/v1/products", request2, ProductResponse.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/products?search=LIST_TEST" + uniqueSuffix.substring(1),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("LIST_TEST_1" + uniqueSuffix);
        assertThat(response.getBody()).contains("LIST_TEST_2" + uniqueSuffix);
    }

    @Test
    void listProducts_FilterByStatus() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("STATUS_FILTER" + uniqueSuffix)
                .name("Status Filter Product")
                .build();

        restTemplate.postForEntity("/api/v1/products", request, ProductResponse.class);

        ResponseEntity<String> draftResponse = restTemplate.getForEntity(
                "/api/v1/products?status=DRAFT&search=STATUS_FILTER" + uniqueSuffix.substring(1),
                String.class);

        assertThat(draftResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(draftResponse.getBody()).contains("STATUS_FILTER" + uniqueSuffix);
    }

    @Test
    void updateProduct_Success() {
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
                .code("UPDATE_TEST")
                .name("Original Name")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                createRequest,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();
        Long version = createResponse.getBody().getVersion();

        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .name("Updated Name")
                .description("New Description")
                .version(version)
                .build();

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/v1/products/" + productId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Name");
        assertThat(response.getBody().getDescription()).isEqualTo("New Description");
    }

    @Test
    void deleteProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("DELETE_TEST")
                .name("Delete Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/products/" + productId,
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/v1/products/" + productId,
                String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void activateProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("ACTIVATE_TEST")
                .name("Activate Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<ProductResponse> activateResponse = restTemplate.postForEntity(
                "/api/v1/products/" + productId + "/activate",
                null,
                ProductResponse.class);

        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activateResponse.getBody().getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void archiveProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("ARCHIVE_TEST")
                .name("Archive Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<ProductResponse> archiveResponse = restTemplate.postForEntity(
                "/api/v1/products/" + productId + "/archive",
                null,
                ProductResponse.class);

        assertThat(archiveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(archiveResponse.getBody().getStatus()).isEqualTo(EntityStatus.ARCHIVED);
    }

    @Test
    void restoreProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("RESTORE_TEST")
                .name("Restore Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        restTemplate.postForEntity("/api/v1/products/" + productId + "/archive", null, ProductResponse.class);

        ResponseEntity<ProductResponse> restoreResponse = restTemplate.postForEntity(
                "/api/v1/products/" + productId + "/restore",
                null,
                ProductResponse.class);

        assertThat(restoreResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restoreResponse.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void lifecycleTransitions_InvalidTransitions() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("INVALID_TRANSITION")
                .name("Invalid Transition Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<String> restoreFromDraft = restTemplate.postForEntity(
                "/api/v1/products/" + productId + "/restore",
                null,
                String.class);

        assertThat(restoreFromDraft.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getProductTree_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("TREE_TEST" + uniqueSuffix)
                .name("Tree Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<ProductTreeResponse> response = restTemplate.getForEntity(
                "/api/v1/products/" + productId + "/tree",
                ProductTreeResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(productId);
        assertThat(response.getBody().getCode()).isEqualTo("TREE_TEST" + uniqueSuffix);
    }

    @Test
    void getProductRules_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("RULES_TEST" + uniqueSuffix)
                .name("Rules Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/products/" + productId + "/rules",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getProductAccounts_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("ACCOUNTS_TEST" + uniqueSuffix)
                .name("Accounts Test Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/products/" + productId + "/accounts",
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void cloneProduct_Success() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("CLONE_SOURCE" + uniqueSuffix)
                .name("Clone Source Product")
                .description("Original Description")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        CloneRequest cloneRequest = CloneRequest.builder()
                .newCode("CLONE_TARGET" + uniqueSuffix)
                .newName("Cloned Product")
                .build();

        ResponseEntity<ProductResponse> cloneResponse = restTemplate.postForEntity(
                "/api/v1/products/" + productId + "/clone",
                cloneRequest,
                ProductResponse.class);

        assertThat(cloneResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cloneResponse.getBody()).isNotNull();
        assertThat(cloneResponse.getBody().getCode()).isEqualTo("CLONE_TARGET" + uniqueSuffix);
        assertThat(cloneResponse.getBody().getName()).isEqualTo("Cloned Product");
        assertThat(cloneResponse.getBody().getDescription()).isEqualTo("Original Description");
        assertThat(cloneResponse.getBody().getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(cloneResponse.getBody().getId()).isNotEqualTo(productId);
    }

    @Test
    void cloneProduct_WithAutoGeneratedCode() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        ProductCreateRequest request = ProductCreateRequest.builder()
                .code("AUTO_CLONE" + uniqueSuffix)
                .name("Auto Clone Product")
                .build();

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products",
                request,
                ProductResponse.class);

        Long productId = createResponse.getBody().getId();

        ResponseEntity<ProductResponse> cloneResponse = restTemplate.postForEntity(
                "/api/v1/products/" + productId + "/clone",
                new CloneRequest(),
                ProductResponse.class);

        assertThat(cloneResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cloneResponse.getBody()).isNotNull();
        assertThat(cloneResponse.getBody().getCode()).contains("AUTO_CLONE" + uniqueSuffix);
        assertThat(cloneResponse.getBody().getCode()).contains("-copy-");
    }
}
