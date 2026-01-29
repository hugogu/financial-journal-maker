package com.financial.coa.controller;

import com.financial.coa.dto.ErrorResponse;
import com.financial.coa.dto.MappingCreateRequest;
import com.financial.coa.dto.MappingResponse;
import com.financial.coa.dto.MappingUpdateRequest;
import com.financial.coa.service.AccountMappingService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST controller for managing account to Formance Ledger mappings.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts/mappings")
@RequiredArgsConstructor
@Tag(name = "Account Mappings", description = "COA to Formance Ledger mapping operations")
public class AccountMappingController {
    
    private final AccountMappingService mappingService;
    
    @PostMapping
    @Operation(summary = "Create account mapping", 
        description = "Creates a mapping between a COA account code and a Formance Ledger account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Mapping created successfully",
            content = @Content(schema = @Schema(implementation = MappingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or mapping already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MappingResponse> createMapping(
            @Valid @RequestBody MappingCreateRequest request) {
        log.info("POST /api/v1/accounts/mappings - accountCode: {}", request.getAccountCode());
        
        MappingResponse response = mappingService.createMapping(request);
        
        URI location = URI.create("/api/v1/accounts/mappings/" + response.getAccountCode());
        return ResponseEntity.created(location).body(response);
    }
    
    @GetMapping("/{accountCode}")
    @Operation(summary = "Get mapping by account code",
        description = "Retrieves the Formance Ledger mapping for a specific COA account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapping found",
            content = @Content(schema = @Schema(implementation = MappingResponse.class))),
        @ApiResponse(responseCode = "404", description = "Mapping not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MappingResponse> getMapping(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String accountCode) {
        log.debug("GET /api/v1/accounts/mappings/{}", accountCode);
        
        MappingResponse response = mappingService.getMapping(accountCode);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "List all mappings", description = "Retrieves all account mappings with pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mappings retrieved successfully")
    })
    public ResponseEntity<Page<MappingResponse>> listMappings(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("GET /api/v1/accounts/mappings - page: {}", pageable.getPageNumber());
        
        Page<MappingResponse> response = mappingService.listMappings(pageable);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{accountCode}")
    @Operation(summary = "Update account mapping",
        description = "Updates the Formance Ledger account path for an existing mapping")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mapping updated successfully",
            content = @Content(schema = @Schema(implementation = MappingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Mapping not found"),
        @ApiResponse(responseCode = "409", description = "Version conflict")
    })
    public ResponseEntity<MappingResponse> updateMapping(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String accountCode,
            @Valid @RequestBody MappingUpdateRequest request) {
        log.info("PUT /api/v1/accounts/mappings/{}", accountCode);
        
        MappingResponse response = mappingService.updateMapping(accountCode, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{accountCode}")
    @Operation(summary = "Delete account mapping",
        description = "Removes the Formance Ledger mapping for an account")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Mapping deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Mapping not found")
    })
    public ResponseEntity<Void> deleteMapping(
            @Parameter(description = "Account code", example = "1000")
            @PathVariable String accountCode) {
        log.info("DELETE /api/v1/accounts/mappings/{}", accountCode);
        
        mappingService.deleteMapping(accountCode);
        return ResponseEntity.noContent().build();
    }
}
