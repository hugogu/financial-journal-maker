package com.financial.rules.controller;

import com.financial.BaseIntegrationTest;
import com.financial.coa.dto.ErrorResponse;
import com.financial.rules.domain.EntryType;
import com.financial.rules.domain.RuleStatus;
import com.financial.rules.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AccountingRuleController.
 * Tests all accounting rule operations including lifecycle management.
 */
@DisplayName("AccountingRuleController Integration Tests")
class AccountingRuleControllerIntegrationTest extends BaseIntegrationTest {

    private String rulesUrl() {
        return baseUrl + "/api/v1/rules";
    }

    private RuleCreateRequest createValidRuleRequest(String code, String name) {
        RuleCreateRequest request = new RuleCreateRequest();
        request.setCode(code);
        request.setName(name);
        request.setDescription("Test rule description");

        // Create entry template
        EntryTemplateRequest template = new EntryTemplateRequest();
        template.setDescription("Test entry template");
        
        List<VariableDefinition> variables = new ArrayList<>();
        variables.add(VariableDefinition.builder()
                .name("amount")
                .type(ExpressionType.DECIMAL)
                .description("Transaction amount")
                .build());
        variables.add(VariableDefinition.builder()
                .name("account")
                .type(ExpressionType.STRING)
                .description("Account code")
                .build());
        template.setVariableSchema(variables);
        
        List<EntryLineRequest> lines = new ArrayList<>();
        lines.add(EntryLineRequest.builder()
                .accountCode("1000")
                .entryType(EntryType.DEBIT)
                .amountExpression("amount")
                .memoTemplate("Debit entry")
                .build());
        lines.add(EntryLineRequest.builder()
                .accountCode("2000")
                .entryType(EntryType.CREDIT)
                .amountExpression("amount")
                .memoTemplate("Credit entry")
                .build());
        template.setLines(lines);
        request.setEntryTemplate(template);

        return request;
    }

    @Nested
    @DisplayName("POST /api/v1/rules - Create Rule")
    class CreateRuleTests {

        @Test
        @DisplayName("Should create rule successfully with valid data")
        void createRule_WithValidData_ReturnsCreated() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");

            ResponseEntity<RuleResponse> response = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("RULE001");
            assertThat(response.getBody().getName()).isEqualTo("Test Rule");
            assertThat(response.getBody().getStatus()).isEqualTo(RuleStatus.DRAFT);
            assertThat(response.getBody().getCurrentVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reject duplicate rule code")
        void createRule_WithDuplicateCode_ReturnsConflict() {
            RuleCreateRequest first = createValidRuleRequest("RULE001", "First Rule");
            restTemplate.postForEntity(rulesUrl(), first, RuleResponse.class);

            RuleCreateRequest duplicate = createValidRuleRequest("RULE001", "Duplicate Rule");
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    rulesUrl(), duplicate, ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rules - List Rules")
    class ListRulesTests {

        @Test
        @DisplayName("Should return paginated list of rules")
        void listRules_ReturnsPagedResults() {
            // Create multiple rules
            for (int i = 1; i <= 5; i++) {
                RuleCreateRequest request = createValidRuleRequest("RULE00" + i, "Rule " + i);
                restTemplate.postForEntity(rulesUrl(), request, RuleResponse.class);
            }

            ResponseEntity<RestPageResponse<RuleSummaryResponse>> response = restTemplate.exchange(
                    rulesUrl() + "?size=3",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RestPageResponse<RuleSummaryResponse>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(3);
            assertThat(response.getBody().getTotalElements()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should filter rules by status")
        void listRules_FilterByStatus_ReturnsFilteredResults() {
            // Create DRAFT rule
            RuleCreateRequest draft = createValidRuleRequest("DRAFT001", "Draft Rule");
            restTemplate.postForEntity(rulesUrl(), draft, RuleResponse.class);

            ResponseEntity<RestPageResponse<RuleSummaryResponse>> response = restTemplate.exchange(
                    rulesUrl() + "?status=DRAFT",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RestPageResponse<RuleSummaryResponse>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).allMatch(r -> r.getStatus() == RuleStatus.DRAFT);
        }

        @Test
        @DisplayName("Should search rules by code or name")
        void listRules_SearchByName_ReturnsMatchingResults() {
            RuleCreateRequest rule1 = createValidRuleRequest("SALES001", "Sales Revenue Rule");
            RuleCreateRequest rule2 = createValidRuleRequest("EXPENSE001", "Expense Rule");
            restTemplate.postForEntity(rulesUrl(), rule1, RuleResponse.class);
            restTemplate.postForEntity(rulesUrl(), rule2, RuleResponse.class);

            ResponseEntity<RestPageResponse<RuleSummaryResponse>> response = restTemplate.exchange(
                    rulesUrl() + "?search=SALES",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RestPageResponse<RuleSummaryResponse>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getCode()).isEqualTo("SALES001");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rules/{id} - Get Rule by ID")
    class GetRuleTests {

        @Test
        @DisplayName("Should return rule by ID")
        void getRule_WithValidId_ReturnsRule() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);

            Long ruleId = created.getBody().getId();

            ResponseEntity<RuleResponse> response = restTemplate.getForEntity(
                    rulesUrl() + "/" + ruleId, RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getCode()).isEqualTo("RULE001");
        }

        @Test
        @DisplayName("Should return 404 for non-existent rule")
        void getRule_WithInvalidId_ReturnsNotFound() {
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                    rulesUrl() + "/99999", ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rules/code/{code} - Get Rule by Code")
    class GetRuleByCodeTests {

        @Test
        @DisplayName("Should return rule by code")
        void getRuleByCode_WithValidCode_ReturnsRule() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            restTemplate.postForEntity(rulesUrl(), request, RuleResponse.class);

            ResponseEntity<RuleResponse> response = restTemplate.getForEntity(
                    rulesUrl() + "/code/RULE001", RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getCode()).isEqualTo("RULE001");
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/rules/{id} - Update Rule")
    class UpdateRuleTests {

        @Test
        @DisplayName("Should update rule name and description")
        void updateRule_WithValidData_ReturnsUpdated() {
            RuleCreateRequest create = createValidRuleRequest("RULE001", "Original Name");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), create, RuleResponse.class);

            Long ruleId = created.getBody().getId();

            RuleUpdateRequest update = new RuleUpdateRequest();
            update.setName("Updated Name");
            update.setDescription("Updated description");
            update.setVersion(created.getBody().getVersion());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RuleUpdateRequest> entity = new HttpEntity<>(update, headers);

            ResponseEntity<RuleResponse> response = restTemplate.exchange(
                    rulesUrl() + "/" + ruleId,
                    HttpMethod.PUT,
                    entity,
                    RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getName()).isEqualTo("Updated Name");
            assertThat(response.getBody().getDescription()).isEqualTo("Updated description");
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/rules/{id} - Delete Rule")
    class DeleteRuleTests {

        @Test
        @DisplayName("Should delete DRAFT rule")
        void deleteRule_DraftStatus_ReturnsNoContent() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);

            Long ruleId = created.getBody().getId();

            ResponseEntity<Void> response = restTemplate.exchange(
                    rulesUrl() + "/" + ruleId,
                    HttpMethod.DELETE,
                    null,
                    Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("Should not delete ACTIVE rule")
        void deleteRule_ActiveStatus_ReturnsConflict() {
            // Create and activate rule
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            // Activate rule
            restTemplate.postForEntity(rulesUrl() + "/" + ruleId + "/activate", null, RuleResponse.class);

            // Try to delete
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    rulesUrl() + "/" + ruleId,
                    HttpMethod.DELETE,
                    null,
                    ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    @DisplayName("Rule Lifecycle Operations")
    class LifecycleTests {

        @Test
        @DisplayName("Should activate DRAFT rule")
        void activateRule_FromDraft_ReturnsActive() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            ResponseEntity<RuleResponse> response = restTemplate.postForEntity(
                    rulesUrl() + "/" + ruleId + "/activate", null, RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getStatus()).isEqualTo(RuleStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should archive rule")
        void archiveRule_ReturnsArchived() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            ResponseEntity<RuleResponse> response = restTemplate.postForEntity(
                    rulesUrl() + "/" + ruleId + "/archive", null, RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getStatus()).isEqualTo(RuleStatus.ARCHIVED);
        }

        @Test
        @DisplayName("Should restore ARCHIVED rule to DRAFT")
        void restoreRule_FromArchived_ReturnsDraft() {
            // Create and archive rule
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();
            restTemplate.postForEntity(rulesUrl() + "/" + ruleId + "/archive", null, RuleResponse.class);

            // Restore
            ResponseEntity<RuleResponse> response = restTemplate.postForEntity(
                    rulesUrl() + "/" + ruleId + "/restore", null, RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getStatus()).isEqualTo(RuleStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/rules/{id}/clone - Clone Rule")
    class CloneRuleTests {

        @Test
        @DisplayName("Should clone rule successfully")
        void cloneRule_WithValidData_ReturnsCloned() {
            RuleCreateRequest original = createValidRuleRequest("ORIGINAL001", "Original Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), original, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            CloneRuleRequest clone = new CloneRuleRequest();
            clone.setNewCode("CLONE001");
            clone.setNewName("Cloned Rule");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CloneRuleRequest> entity = new HttpEntity<>(clone, headers);

            ResponseEntity<RuleResponse> response = restTemplate.postForEntity(
                    rulesUrl() + "/" + ruleId + "/clone", entity, RuleResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getCode()).isEqualTo("CLONE001");
            assertThat(response.getBody().getName()).isEqualTo("Cloned Rule");
            assertThat(response.getBody().getStatus()).isEqualTo(RuleStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("Version Management")
    class VersionTests {

        @Test
        @DisplayName("Should list rule versions")
        void listVersions_ReturnsVersionHistory() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            ResponseEntity<RestPageResponse<VersionSummaryResponse>> response = restTemplate.exchange(
                    rulesUrl() + "/" + ruleId + "/versions",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RestPageResponse<VersionSummaryResponse>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("Should get specific version")
        void getVersion_WithValidNumber_ReturnsVersion() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            ResponseEntity<VersionResponse> response = restTemplate.getForEntity(
                    rulesUrl() + "/" + ruleId + "/versions/1", VersionResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getVersionNumber()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/rules/{id}/generate - Generate Numscript")
    class GenerateNumscriptTests {

        @Test
        @DisplayName("Should generate Numscript from rule")
        void generateNumscript_WithValidRule_ReturnsScript() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            ResponseEntity<GenerationResponse> response = restTemplate.postForEntity(
                    rulesUrl() + "/" + ruleId + "/generate", null, GenerationResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getNumscript()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/rules/validate-expression - Validate Expression")
    class ValidateExpressionTests {

        @Test
        @DisplayName("Should validate valid expression")
        void validateExpression_ValidExpression_ReturnsValid() {
            ExpressionValidationRequest request = new ExpressionValidationRequest();
            request.setExpression("amount * 0.1");
            
            List<VariableDefinition> schema = new ArrayList<>();
            schema.add(VariableDefinition.builder()
                    .name("amount")
                    .type(ExpressionType.DECIMAL)
                    .description("Amount")
                    .build());
            request.setVariableSchema(schema);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ExpressionValidationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ExpressionValidationResponse> response = restTemplate.postForEntity(
                    rulesUrl() + "/validate-expression", entity, ExpressionValidationResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/rules/{id}/simulate - Simulate Rule")
    class SimulateRuleTests {

        @Test
        @DisplayName("Should simulate rule execution")
        void simulateRule_WithEventData_ReturnsSimulation() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            SimulationRequest simulation = new SimulationRequest();
            simulation.setEventData(Map.of(
                    "amount", 1000.00,
                    "account", "1000"
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SimulationRequest> entity = new HttpEntity<>(simulation, headers);

            ResponseEntity<SimulationResponse> response = restTemplate.postForEntity(
                    rulesUrl() + "/" + ruleId + "/simulate", entity, SimulationResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/rules/{id}/references - Get Rule References")
    class GetReferencesTests {

        @Test
        @DisplayName("Should return rule references")
        void getRuleReferences_ReturnsReferences() {
            RuleCreateRequest request = createValidRuleRequest("RULE001", "Test Rule");
            ResponseEntity<RuleResponse> created = restTemplate.postForEntity(
                    rulesUrl(), request, RuleResponse.class);
            Long ruleId = created.getBody().getId();

            ResponseEntity<RuleReferenceResponse> response = restTemplate.getForEntity(
                    rulesUrl() + "/" + ruleId + "/references", RuleReferenceResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }

    /**
     * Helper class for deserializing paginated responses.
     */
    static class RestPageResponse<T> {
        private List<T> content;
        private long totalElements;
        private int totalPages;

        public List<T> getContent() { return content; }
        public void setContent(List<T> content) { this.content = content; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
