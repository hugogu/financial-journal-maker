package com.financial.ai.controller;

import com.financial.ai.domain.DesignPhase;
import com.financial.ai.dto.PromptRequest;
import com.financial.ai.dto.PromptResponse;
import com.financial.ai.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/prompts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prompt Templates", description = "Admin endpoints for managing AI prompt templates")
public class PromptController {

    private final PromptService promptService;

    @GetMapping
    @Operation(summary = "List all prompts", description = "Get all prompt templates")
    @ApiResponse(responseCode = "200", description = "List of prompt templates")
    public ResponseEntity<List<PromptResponse>> getAllPrompts() {
        return ResponseEntity.ok(promptService.getAllPrompts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prompt by ID", description = "Get a specific prompt template by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prompt found"),
            @ApiResponse(responseCode = "404", description = "Prompt not found")
    })
    public ResponseEntity<PromptResponse> getPromptById(
            @Parameter(description = "Prompt ID") @PathVariable Long id) {
        return ResponseEntity.ok(promptService.getPromptById(id));
    }

    @GetMapping("/phase/{phase}")
    @Operation(summary = "List prompts by phase", description = "Get all prompt templates for a design phase")
    @ApiResponse(responseCode = "200", description = "List of prompts for the phase")
    public ResponseEntity<List<PromptResponse>> getPromptsByPhase(
            @Parameter(description = "Design phase") @PathVariable DesignPhase phase) {
        return ResponseEntity.ok(promptService.getPromptsByPhase(phase));
    }

    @GetMapping("/phase/{phase}/active")
    @Operation(summary = "Get active prompt for phase", description = "Get the currently active prompt for a design phase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active prompt found"),
            @ApiResponse(responseCode = "204", description = "No active prompt for this phase")
    })
    public ResponseEntity<PromptResponse> getActivePromptByPhase(
            @Parameter(description = "Design phase") @PathVariable DesignPhase phase) {
        PromptResponse response = promptService.getActivePromptByPhase(phase);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{name}")
    @Operation(summary = "Get version history", description = "Get all versions of a prompt by name")
    @ApiResponse(responseCode = "200", description = "Version history")
    public ResponseEntity<List<PromptResponse>> getVersionHistory(
            @Parameter(description = "Prompt name") @PathVariable String name) {
        return ResponseEntity.ok(promptService.getPromptVersionHistory(name));
    }

    @PostMapping
    @Operation(summary = "Create prompt", description = "Create a new prompt template")
    @ApiResponse(responseCode = "201", description = "Prompt created")
    public ResponseEntity<PromptResponse> createPrompt(
            @Valid @RequestBody PromptRequest request) {
        PromptResponse response = promptService.createPrompt(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update prompt", description = "Update a prompt (creates a new version)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prompt updated (new version created)"),
            @ApiResponse(responseCode = "404", description = "Prompt not found")
    })
    public ResponseEntity<PromptResponse> updatePrompt(
            @Parameter(description = "Prompt ID") @PathVariable Long id,
            @Valid @RequestBody PromptRequest request) {
        return ResponseEntity.ok(promptService.updatePrompt(id, request));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate prompt", description = "Set a prompt as the active one for its phase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prompt activated"),
            @ApiResponse(responseCode = "404", description = "Prompt not found")
    })
    public ResponseEntity<PromptResponse> activatePrompt(
            @Parameter(description = "Prompt ID") @PathVariable Long id) {
        return ResponseEntity.ok(promptService.activatePrompt(id));
    }

    @PostMapping("/rollback")
    @Operation(summary = "Rollback to version", description = "Rollback a prompt to a specific version (creates a new version with old content)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rollback successful"),
            @ApiResponse(responseCode = "404", description = "Prompt or version not found")
    })
    public ResponseEntity<PromptResponse> rollbackToVersion(
            @Parameter(description = "Prompt name") @RequestParam String name,
            @Parameter(description = "Version to rollback to") @RequestParam Integer version) {
        return ResponseEntity.ok(promptService.rollbackToVersion(name, version));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete prompt", description = "Delete a prompt (cannot delete active prompts)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Prompt deleted"),
            @ApiResponse(responseCode = "404", description = "Prompt not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete active prompt")
    })
    public ResponseEntity<Void> deletePrompt(
            @Parameter(description = "Prompt ID") @PathVariable Long id) {
        promptService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    @Operation(summary = "Initialize default prompts", description = "Create default prompt templates if none exist")
    @ApiResponse(responseCode = "200", description = "Default prompts initialized")
    public ResponseEntity<Void> initializeDefaults() {
        promptService.initializeDefaultPrompts();
        return ResponseEntity.ok().build();
    }
}
