package com.financial.coa.controller;

import com.financial.coa.dto.ErrorResponse;
import com.financial.coa.dto.ImportErrorResponse;
import com.financial.coa.dto.ImportJobResponse;
import com.financial.coa.service.AccountImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

/**
 * REST controller for batch import operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts/import")
@RequiredArgsConstructor
@Tag(name = "Account Import", description = "Batch import operations for chart of accounts")
public class AccountImportController {
    
    private final AccountImportService importService;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import accounts from file",
        description = "Imports accounts from an Excel (.xlsx, .xls) or CSV file. " +
                     "File must have headers: code, name. Optional: parent_code, description, shared")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Import completed successfully",
            content = @Content(schema = @Schema(implementation = ImportJobResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file format or validation errors",
            content = @Content(schema = @Schema(implementation = ImportErrorResponse.class))),
        @ApiResponse(responseCode = "413", description = "File too large (max 10MB)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ImportJobResponse> importAccounts(
            @Parameter(description = "Excel or CSV file containing accounts", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "If true, only validates file without importing")
            @RequestParam(value = "validateOnly", defaultValue = "false") Boolean validateOnly) 
            throws IOException {
        
        log.info("POST /api/v1/accounts/import - file: {}, size: {}, validateOnly: {}",
                file.getOriginalFilename(), file.getSize(), validateOnly);
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                    file.getSize(), MAX_FILE_SIZE));
        }
        
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (validateOnly) {
            // Just validate, don't import
            var errors = importService.validateImportFile(file);
            if (!errors.isEmpty()) {
                throw new com.financial.coa.exception.InvalidImportFileException(
                    "Validation found " + errors.size() + " error(s)",
                    errors.stream()
                        .map(e -> String.format("Row %d: %s - %s", e.getRow(), e.getField(), e.getMessage()))
                        .toList()
                );
            }
            // Return a mock response for validate-only mode
            return ResponseEntity.ok(ImportJobResponse.builder()
                    .status(com.financial.coa.domain.ImportJob.ImportStatus.COMPLETED)
                    .fileName(file.getOriginalFilename())
                    .build());
        }
        
        // Perform actual import
        ImportJobResponse response = importService.performImport(file);
        
        URI location = URI.create("/api/v1/accounts/import/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }
    
    @GetMapping("/{jobId}")
    @Operation(summary = "Get import job status",
        description = "Retrieves the status of a specific import job")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Job status retrieved",
            content = @Content(schema = @Schema(implementation = ImportJobResponse.class))),
        @ApiResponse(responseCode = "404", description = "Import job not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ImportJobResponse> getImportJob(
            @Parameter(description = "Import job ID")
            @PathVariable Long jobId) {
        log.debug("GET /api/v1/accounts/import/{}", jobId);
        
        ImportJobResponse response = importService.getImportJob(jobId);
        return ResponseEntity.ok(response);
    }
}
