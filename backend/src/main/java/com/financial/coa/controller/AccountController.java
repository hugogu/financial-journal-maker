package com.financial.coa.controller;

import com.financial.coa.dto.*;
import com.financial.coa.service.AccountService;
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

import java.net.URI;
import java.util.List;

/**
 * REST controller for account management operations.
 * Provides endpoints for CRUD operations, tree navigation, and reference management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Chart of accounts CRUD operations")
public class AccountController {
    
    private final AccountService accountService;
    
    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new account in the chart of accounts")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Account code already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountCreateRequest request) {
        log.info("POST /api/v1/accounts - code: {}", request.getCode());
        
        AccountResponse response = accountService.createAccount(request);
        
        URI location = URI.create("/api/v1/accounts/" + response.getCode());
        return ResponseEntity.created(location).body(response);
    }
    
    @GetMapping("/{code}")
    @Operation(summary = "Get account by code", description = "Retrieves a single account by its unique code")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "Unique account code", example = "1000")
            @PathVariable String code) {
        log.debug("GET /api/v1/accounts/{}", code);
        
        AccountResponse response = accountService.getAccountByCode(code);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "List all accounts", description = "Retrieves paginated list of accounts")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    public ResponseEntity<Page<AccountResponse>> listAccounts(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Filter by shared flag")
            @RequestParam(required = false) Boolean shared) {
        log.debug("GET /api/v1/accounts - page: {}, shared: {}", pageable.getPageNumber(), shared);
        
        Page<AccountResponse> response = accountService.listAccounts(pageable, shared);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tree")
    @Operation(summary = "Get account tree", description = "Retrieves hierarchical tree structure of accounts")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tree structure retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Root account not found if rootCode specified")
    })
    public ResponseEntity<List<AccountTreeNode>> getAccountTree(
            @Parameter(description = "Optional root account code for subtree", example = "1000")
            @RequestParam(required = false) String rootCode) {
        log.debug("GET /api/v1/accounts/tree - rootCode: {}", rootCode);
        
        List<AccountTreeNode> tree = accountService.getAccountTree(rootCode);
        return ResponseEntity.ok(tree);
    }
    
    @PutMapping("/{code}")
    @Operation(summary = "Update account", 
        description = "Updates account name, description, or parent. Code cannot be changed if account is referenced.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account updated successfully",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Version conflict or account is referenced")
    })
    public ResponseEntity<AccountResponse> updateAccount(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String code,
            @Valid @RequestBody AccountUpdateRequest request) {
        log.info("PUT /api/v1/accounts/{}", code);
        
        AccountResponse response = accountService.updateAccount(code, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{code}")
    @Operation(summary = "Delete account", 
        description = "Deletes account if it has no children and is not referenced")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Account has children or is referenced")
    })
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String code) {
        log.info("DELETE /api/v1/accounts/{}", code);
        
        accountService.deleteAccount(code);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{code}/references")
    @Operation(summary = "List account references", 
        description = "Retrieves all references to this account from rules and scenarios")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "References retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<List<AccountReferenceResponse>> getAccountReferences(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String code) {
        log.debug("GET /api/v1/accounts/{}/references", code);
        
        List<AccountReferenceResponse> references = accountService.getAccountReferences(code);
        return ResponseEntity.ok(references);
    }
    
    @PostMapping("/references")
    @Operation(summary = "Create account reference", 
        description = "Marks an account as referenced by a rule or scenario (internal use)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Reference created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountReferenceResponse> createReference(
            @Valid @RequestBody ReferenceCreateRequest request) {
        log.info("POST /api/v1/accounts/references - {} -> {}", 
            request.getAccountCode(), request.getReferenceSourceId());
        
        AccountReferenceResponse response = accountService.createReference(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/references/{referenceId}")
    @Operation(summary = "Delete account reference",
        description = "Removes a reference when rule or scenario is deleted (internal use)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reference deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Reference not found")
    })
    public ResponseEntity<Void> deleteReference(
            @Parameter(description = "Reference ID")
            @PathVariable Long referenceId) {
        log.info("DELETE /api/v1/accounts/references/{}", referenceId);
        
        accountService.deleteReference(referenceId);
        return ResponseEntity.noContent().build();
    }
    
    // Cross-scenario endpoints (T053-T054)
    
    @PatchMapping("/{code}/shared")
    @Operation(summary = "Mark account as shared",
        description = "Marks an account as shared across scenarios or removes the shared flag")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account updated successfully",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Cannot modify shared account in use")
    })
    public ResponseEntity<AccountResponse> markAccountAsShared(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String code,
            @Parameter(description = "Whether the account should be shared across scenarios")
            @RequestParam Boolean shared) {
        log.info("PATCH /api/v1/accounts/{}/shared?shared={}", code, shared);
        
        AccountResponse response = accountService.markAccountAsShared(code, shared);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{code}/scenarios")
    @Operation(summary = "List account scenarios",
        description = "Retrieves list of scenarios where this account is used")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Scenarios retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<List<String>> getAccountScenarios(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String code) {
        log.debug("GET /api/v1/accounts/{}/scenarios", code);
        
        List<String> scenarios = accountService.listAccountScenarios(code);
        return ResponseEntity.ok(scenarios);
    }
}
