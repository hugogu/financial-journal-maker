package com.financial.rules.controller;

import com.financial.rules.domain.RuleStatus;
import com.financial.rules.dto.*;
import com.financial.rules.service.AccountingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounting Rules", description = "Accounting Rules Management API")
public class AccountingRuleController {

    private final AccountingRuleService ruleService;

    @PostMapping
    @Operation(summary = "Create a new accounting rule", description = "Creates a new accounting rule with entry template and optional trigger conditions")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Rule code already exists")
    })
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody RuleCreateRequest request) {
        log.info("REST request to create accounting rule: {}", request.getCode());
        RuleResponse response = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all accounting rules", description = "Returns a paginated list of accounting rules with optional filters")
    @ApiResponse(responseCode = "200", description = "List of rules retrieved successfully")
    public ResponseEntity<Page<RuleSummaryResponse>> listRules(
            @Parameter(description = "Filter by rule status") @RequestParam(required = false) RuleStatus status,
            @Parameter(description = "Filter by shared flag") @RequestParam(required = false) Boolean shared,
            @Parameter(description = "Search by code or name") @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to list accounting rules");
        Page<RuleSummaryResponse> rules = ruleService.listRules(status, shared, search, pageable);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accounting rule by ID", description = "Returns a single accounting rule with all details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule found"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<RuleResponse> getRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        log.debug("REST request to get accounting rule: {}", id);
        RuleResponse response = ruleService.getRule(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get accounting rule by code", description = "Returns a single accounting rule by its business code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule found"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<RuleResponse> getRuleByCode(
            @Parameter(description = "Rule code") @PathVariable String code) {
        log.debug("REST request to get accounting rule by code: {}", code);
        RuleResponse response = ruleService.getRuleByCode(code);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an accounting rule", description = "Updates an existing accounting rule. Requires version for optimistic locking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "409", description = "Version conflict or invalid state transition")
    })
    public ResponseEntity<RuleResponse> updateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Valid @RequestBody RuleUpdateRequest request) {
        log.info("REST request to update accounting rule: {}", id);
        RuleResponse response = ruleService.updateRule(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an accounting rule", description = "Deletes an accounting rule. Cannot delete ACTIVE rules.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete rule in current state")
    })
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        log.info("REST request to delete accounting rule: {}", id);
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/clone")
    @Operation(summary = "Clone an accounting rule", description = "Creates a copy of an existing rule with a new code and name")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rule cloned successfully"),
            @ApiResponse(responseCode = "404", description = "Source rule not found"),
            @ApiResponse(responseCode = "409", description = "New rule code already exists")
    })
    public ResponseEntity<RuleResponse> cloneRule(
            @Parameter(description = "Source rule ID") @PathVariable Long id,
            @Valid @RequestBody CloneRuleRequest request) {
        log.info("REST request to clone accounting rule {} to {}", id, request.getNewCode());
        RuleResponse response = ruleService.cloneRule(id, request.getNewCode(), request.getNewName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate an accounting rule", description = "Transitions a DRAFT rule to ACTIVE state after validation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule activated successfully"),
            @ApiResponse(responseCode = "400", description = "Rule validation failed"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition")
    })
    public ResponseEntity<RuleResponse> activateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        log.info("REST request to activate accounting rule: {}", id);
        RuleResponse response = ruleService.activateRule(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive an accounting rule", description = "Transitions a DRAFT or ACTIVE rule to ARCHIVED state")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule archived successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition")
    })
    public ResponseEntity<RuleResponse> archiveRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        log.info("REST request to archive accounting rule: {}", id);
        RuleResponse response = ruleService.archiveRule(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore an archived rule", description = "Transitions an ARCHIVED rule back to DRAFT state")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule restored successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition")
    })
    public ResponseEntity<RuleResponse> restoreRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        log.info("REST request to restore accounting rule: {}", id);
        RuleResponse response = ruleService.restoreRule(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "List rule versions", description = "Returns version history for an accounting rule")
    @ApiResponse(responseCode = "200", description = "Version list retrieved successfully")
    public ResponseEntity<Page<VersionSummaryResponse>> listVersions(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to list versions for rule: {}", id);
        Page<VersionSummaryResponse> versions = ruleService.listVersions(id, pageable);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    @Operation(summary = "Get specific version", description = "Returns details of a specific rule version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Version found"),
            @ApiResponse(responseCode = "404", description = "Rule or version not found")
    })
    public ResponseEntity<VersionResponse> getVersion(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Parameter(description = "Version number") @PathVariable Integer versionNumber) {
        log.debug("REST request to get version {} for rule: {}", versionNumber, id);
        VersionResponse version = ruleService.getVersion(id, versionNumber);
        return ResponseEntity.ok(version);
    }

    @PostMapping("/{id}/rollback/{versionNumber}")
    @Operation(summary = "Rollback to version", description = "Restores rule to a previous version state (creates new version)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rollback successful"),
            @ApiResponse(responseCode = "404", description = "Rule or version not found"),
            @ApiResponse(responseCode = "409", description = "Cannot rollback ACTIVE rules")
    })
    public ResponseEntity<RuleResponse> rollbackToVersion(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Parameter(description = "Version number to rollback to") @PathVariable Integer versionNumber) {
        log.info("REST request to rollback rule {} to version {}", id, versionNumber);
        RuleResponse response = ruleService.rollbackToVersion(id, versionNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/generate")
    @Operation(summary = "Generate Numscript DSL", description = "Generates Formance Ledger Numscript DSL from an accounting rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Numscript generated successfully"),
            @ApiResponse(responseCode = "400", description = "Rule cannot be converted to Numscript"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<GenerationResponse> generateNumscript(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        log.info("REST request to generate Numscript for rule: {}", id);
        GenerationResponse response = ruleService.generateNumscript(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-expression")
    @Operation(summary = "Validate expression syntax", description = "Validates an amount expression against a variable schema")
    @ApiResponse(responseCode = "200", description = "Validation result returned")
    public ResponseEntity<ExpressionValidationResponse> validateExpression(
            @Valid @RequestBody ExpressionValidationRequest request) {
        log.debug("REST request to validate expression: {}", request.getExpression());
        ExpressionValidationResponse response = ruleService.validateExpression(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/simulate")
    @Operation(summary = "Simulate rule execution", description = "Simulates rule execution with sample event data to preview journal entries")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulation completed"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<SimulationResponse> simulateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Valid @RequestBody SimulationRequest request) {
        log.info("REST request to simulate rule: {}", id);
        SimulationResponse response = ruleService.simulateRule(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/references")
    @Operation(summary = "Get rule references", description = "Returns scenarios and contexts that reference this rule")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "References retrieved"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<RuleReferenceResponse> getRuleReferences(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        log.debug("REST request to get references for rule: {}", id);
        RuleReferenceResponse response = ruleService.getRuleReferences(id);
        return ResponseEntity.ok(response);
    }
}
