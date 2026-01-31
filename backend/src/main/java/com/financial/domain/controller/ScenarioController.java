package com.financial.domain.controller;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.dto.*;
import com.financial.domain.service.HierarchyService;
import com.financial.domain.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/scenarios")
@RequiredArgsConstructor
@Tag(name = "Scenarios", description = "Scenario management operations")
public class ScenarioController {

    private final ScenarioService scenarioService;
    private final HierarchyService hierarchyService;

    @PostMapping
    @Operation(summary = "Create a new scenario under a product")
    public ResponseEntity<ScenarioResponse> createScenario(
            @Valid @RequestBody ScenarioCreateRequest request) {
        ScenarioResponse response = scenarioService.createScenario(request);
        return ResponseEntity
                .created(URI.create("/api/v1/scenarios/" + response.getId()))
                .body(response);
    }

    @GetMapping
    @Operation(summary = "List scenarios with optional filters")
    public ResponseEntity<Page<ScenarioResponse>> listScenarios(
            @Parameter(description = "Filter by product ID")
            @RequestParam(required = false) Long productId,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) EntityStatus status,
            @Parameter(description = "Search by code or name")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(scenarioService.listScenarios(productId, status, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get scenario by ID")
    public ResponseEntity<ScenarioResponse> getScenario(
            @PathVariable Long id) {
        return ResponseEntity.ok(scenarioService.getScenario(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update scenario")
    public ResponseEntity<ScenarioResponse> updateScenario(
            @PathVariable Long id,
            @Valid @RequestBody ScenarioUpdateRequest request) {
        return ResponseEntity.ok(scenarioService.updateScenario(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete scenario")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteScenario(
            @PathVariable Long id) {
        scenarioService.deleteScenario(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate scenario")
    public ResponseEntity<ScenarioResponse> activateScenario(
            @PathVariable Long id) {
        return ResponseEntity.ok(scenarioService.activateScenario(id));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive scenario")
    public ResponseEntity<ScenarioResponse> archiveScenario(
            @PathVariable Long id) {
        return ResponseEntity.ok(scenarioService.archiveScenario(id));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore archived scenario to draft")
    public ResponseEntity<ScenarioResponse> restoreScenario(
            @PathVariable Long id) {
        return ResponseEntity.ok(scenarioService.restoreScenario(id));
    }

    @GetMapping("/{id}/rules")
    @Operation(summary = "Get all rules associated with this scenario through its transaction types")
    public ResponseEntity<java.util.List<RuleSummary>> getScenarioRules(
            @PathVariable Long id) {
        return ResponseEntity.ok(hierarchyService.getScenarioRules(id));
    }

    @GetMapping("/{id}/accounts")
    @Operation(summary = "Get all accounts used in rules associated with this scenario")
    public ResponseEntity<java.util.List<AccountSummary>> getScenarioAccounts(
            @PathVariable Long id) {
        return ResponseEntity.ok(hierarchyService.getScenarioAccounts(id));
    }

    @PostMapping("/{id}/clone")
    @Operation(summary = "Clone scenario with all transaction types (without rule associations)")
    public ResponseEntity<ScenarioResponse> cloneScenario(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ScenarioCloneRequest request) {
        ScenarioCloneRequest cloneRequest = request != null ? request : new ScenarioCloneRequest();
        ScenarioResponse response = scenarioService.cloneScenario(id, cloneRequest);
        return ResponseEntity
                .created(URI.create("/api/v1/scenarios/" + response.getId()))
                .body(response);
    }
}
