package com.financial.coa.controller;

import com.financial.BaseIntegrationTest;
import com.financial.coa.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AccountMappingController.
 * Tests COA to Formance Ledger mapping operations.
 */
@DisplayName("AccountMappingController Integration Tests")
class AccountMappingControllerIntegrationTest extends BaseIntegrationTest {

    private String mappingsUrl() {
        return baseUrl + "/api/v1/accounts/mappings";
    }

    private String accountsUrl() {
        return baseUrl + "/api/v1/accounts";
    }

    @BeforeEach
    void setUpTestData() {
        // Create a test account for mappings
        AccountCreateRequest account = new AccountCreateRequest();
        account.setCode("1000");
        account.setName("Assets");
        restTemplate.postForEntity(accountsUrl(), account, AccountResponse.class);
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/mappings - Create Mapping")
    class CreateMappingTests {

        @Test
        @DisplayName("Should create mapping successfully")
        void createMapping_WithValidData_ReturnsCreated() {
            MappingCreateRequest request = new MappingCreateRequest();
            request.setAccountCode("1000");
            request.setFormanceLedgerAccount("@world:assets:bank");

            ResponseEntity<MappingResponse> response = restTemplate.postForEntity(
                    mappingsUrl(), request, MappingResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAccountCode()).isEqualTo("1000");
            assertThat(response.getBody().getFormanceLedgerAccount()).isEqualTo("@world:assets:bank");
        }

        @Test
        @DisplayName("Should reject mapping for non-existent account")
        void createMapping_WithInvalidAccount_ReturnsNotFound() {
            MappingCreateRequest request = new MappingCreateRequest();
            request.setAccountCode("9999");
            request.setFormanceLedgerAccount("@world:unknown");

            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    mappingsUrl(), request, ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should reject duplicate mapping")
        void createMapping_Duplicate_ReturnsError() {
            // Create first mapping
            MappingCreateRequest request = new MappingCreateRequest();
            request.setAccountCode("1000");
            request.setFormanceLedgerAccount("@world:assets:bank");
            restTemplate.postForEntity(mappingsUrl(), request, MappingResponse.class);

            // Try to create duplicate
            MappingCreateRequest duplicate = new MappingCreateRequest();
            duplicate.setAccountCode("1000");
            duplicate.setFormanceLedgerAccount("@world:assets:other");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    mappingsUrl(), duplicate, String.class);

            // Should return error (either 409 or 500)
            assertThat(response.getStatusCode().isError()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/mappings/{accountCode} - Get Mapping")
    class GetMappingTests {

        @Test
        @DisplayName("Should return mapping by account code")
        void getMapping_WithValidCode_ReturnsMapping() {
            // Create mapping first
            MappingCreateRequest request = new MappingCreateRequest();
            request.setAccountCode("1000");
            request.setFormanceLedgerAccount("@world:assets:bank");
            restTemplate.postForEntity(mappingsUrl(), request, MappingResponse.class);

            ResponseEntity<MappingResponse> response = restTemplate.getForEntity(
                    mappingsUrl() + "/1000", MappingResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getAccountCode()).isEqualTo("1000");
            assertThat(response.getBody().getFormanceLedgerAccount()).isEqualTo("@world:assets:bank");
        }

        @Test
        @DisplayName("Should return error for non-existent mapping")
        void getMapping_WithInvalidCode_ReturnsError() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    mappingsUrl() + "/9999", String.class);

            // Should return error (either 404 or 500)
            assertThat(response.getStatusCode().isError()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/mappings - List Mappings")
    class ListMappingsTests {

        @Test
        @DisplayName("Should return paginated list of mappings")
        void listMappings_ReturnsPagedResults() {
            // Create additional accounts and mappings
            for (int i = 1; i <= 3; i++) {
                AccountCreateRequest account = new AccountCreateRequest();
                account.setCode("200" + i);
                account.setName("Account " + i);
                restTemplate.postForEntity(accountsUrl(), account, AccountResponse.class);

                MappingCreateRequest mapping = new MappingCreateRequest();
                mapping.setAccountCode("200" + i);
                mapping.setFormanceLedgerAccount("@world:account" + i);
                restTemplate.postForEntity(mappingsUrl(), mapping, MappingResponse.class);
            }

            ResponseEntity<RestPageResponse<MappingResponse>> response = restTemplate.exchange(
                    mappingsUrl(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RestPageResponse<MappingResponse>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/accounts/mappings/{accountCode} - Update Mapping")
    class UpdateMappingTests {

        @Test
        @DisplayName("Should update mapping successfully")
        void updateMapping_WithValidData_ReturnsUpdated() {
            // Create mapping first
            MappingCreateRequest create = new MappingCreateRequest();
            create.setAccountCode("1000");
            create.setFormanceLedgerAccount("@world:assets:bank");
            ResponseEntity<MappingResponse> created = restTemplate.postForEntity(
                    mappingsUrl(), create, MappingResponse.class);

            // Update mapping
            MappingUpdateRequest update = new MappingUpdateRequest();
            update.setFormanceLedgerAccount("@world:assets:cash");
            update.setVersion(created.getBody().getVersion());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MappingUpdateRequest> entity = new HttpEntity<>(update, headers);

            ResponseEntity<MappingResponse> response = restTemplate.exchange(
                    mappingsUrl() + "/1000",
                    HttpMethod.PUT,
                    entity,
                    MappingResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getFormanceLedgerAccount()).isEqualTo("@world:assets:cash");
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/accounts/mappings/{accountCode} - Delete Mapping")
    class DeleteMappingTests {

        @Test
        @DisplayName("Should delete mapping successfully")
        void deleteMapping_WithValidCode_ReturnsNoContent() {
            // Create mapping first
            MappingCreateRequest create = new MappingCreateRequest();
            create.setAccountCode("1000");
            create.setFormanceLedgerAccount("@world:assets:bank");
            restTemplate.postForEntity(mappingsUrl(), create, MappingResponse.class);

            ResponseEntity<Void> response = restTemplate.exchange(
                    mappingsUrl() + "/1000",
                    HttpMethod.DELETE,
                    null,
                    Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Verify deleted - should return error
            ResponseEntity<String> verify = restTemplate.getForEntity(
                    mappingsUrl() + "/1000", String.class);
            assertThat(verify.getStatusCode().isError()).isTrue();
        }
    }

    /**
     * Helper class for deserializing paginated responses.
     */
    static class RestPageResponse<T> {
        private List<T> content;
        private long totalElements;

        public List<T> getContent() { return content; }
        public void setContent(List<T> content) { this.content = content; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    }
}
