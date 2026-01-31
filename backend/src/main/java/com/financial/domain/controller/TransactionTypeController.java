package com.financial.domain.controller;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.dto.*;
import com.financial.domain.service.TransactionTypeService;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction-types")
@RequiredArgsConstructor
@Tag(name = "Transaction Types", description = "Transaction type management operations")
public class TransactionTypeController {

    private final TransactionTypeService transactionTypeService;

    @PostMapping
    @Operation(summary = "Create a new transaction type under a scenario")
    public ResponseEntity<TransactionTypeResponse> createTransactionType(
            @Valid @RequestBody TransactionTypeCreateRequest request) {
        TransactionTypeResponse response = transactionTypeService.createTransactionType(request);
        return ResponseEntity
                .created(URI.create("/api/v1/transaction-types/" + response.getId()))
                .body(response);
    }

    @GetMapping
    @Operation(summary = "List transaction types with optional filters")
    public ResponseEntity<Page<TransactionTypeResponse>> listTransactionTypes(
            @Parameter(description = "Filter by scenario ID")
            @RequestParam(required = false) Long scenarioId,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) EntityStatus status,
            @Parameter(description = "Search by code or name")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transactionTypeService.listTransactionTypes(scenarioId, status, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction type by ID")
    public ResponseEntity<TransactionTypeResponse> getTransactionType(
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionTypeService.getTransactionType(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction type")
    public ResponseEntity<TransactionTypeResponse> updateTransactionType(
            @PathVariable Long id,
            @Valid @RequestBody TransactionTypeUpdateRequest request) {
        return ResponseEntity.ok(transactionTypeService.updateTransactionType(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction type")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTransactionType(
            @PathVariable Long id) {
        transactionTypeService.deleteTransactionType(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate transaction type")
    public ResponseEntity<TransactionTypeResponse> activateTransactionType(
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionTypeService.activateTransactionType(id));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive transaction type")
    public ResponseEntity<TransactionTypeResponse> archiveTransactionType(
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionTypeService.archiveTransactionType(id));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore archived transaction type to draft")
    public ResponseEntity<TransactionTypeResponse> restoreTransactionType(
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionTypeService.restoreTransactionType(id));
    }

    @GetMapping("/{id}/rules")
    @Operation(summary = "Get rules associated with this transaction type")
    public ResponseEntity<List<RuleAssociationResponse>> getRuleAssociations(
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionTypeService.getRuleAssociations(id));
    }

    @PostMapping("/{id}/rules")
    @Operation(summary = "Associate a rule with this transaction type")
    public ResponseEntity<RuleAssociationResponse> addRuleAssociation(
            @PathVariable Long id,
            @Valid @RequestBody RuleAssociationRequest request) {
        RuleAssociationResponse response = transactionTypeService.addRuleAssociation(id, request);
        return ResponseEntity
                .created(URI.create("/api/v1/transaction-types/" + id + "/rules/" + response.getRuleId()))
                .body(response);
    }

    @DeleteMapping("/{id}/rules/{ruleId}")
    @Operation(summary = "Remove a rule association from this transaction type")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeRuleAssociation(
            @PathVariable Long id,
            @PathVariable Long ruleId) {
        transactionTypeService.removeRuleAssociation(id, ruleId);
        return ResponseEntity.noContent().build();
    }
}
