package com.financial.ai.controller;

import com.financial.ai.dto.AIConfigRequest;
import com.financial.ai.dto.AIConfigResponse;
import com.financial.ai.dto.AIConfigTestResponse;
import com.financial.ai.service.AIConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ai-config")
@RequiredArgsConstructor
@Tag(name = "AI Configuration", description = "Admin endpoints for managing AI provider configurations")
public class AIConfigController {

    private final AIConfigService configService;

    @GetMapping
    @Operation(summary = "List configurations", description = "Get all AI provider configurations")
    public ResponseEntity<List<AIConfigResponse>> listConfigurations() {
        return ResponseEntity.ok(configService.getAllConfigurations());
    }

    @PostMapping
    @Operation(summary = "Create configuration", description = "Create a new AI provider configuration")
    public ResponseEntity<AIConfigResponse> createConfiguration(
            @Valid @RequestBody AIConfigRequest request) {
        AIConfigResponse response = configService.createConfiguration(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{configId}")
    @Operation(summary = "Get configuration", description = "Get a specific AI configuration by ID")
    public ResponseEntity<AIConfigResponse> getConfiguration(@PathVariable Long configId) {
        return ResponseEntity.ok(configService.getConfiguration(configId));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active configuration", description = "Get the currently active AI configuration")
    public ResponseEntity<AIConfigResponse> getActiveConfiguration() {
        AIConfigResponse active = configService.getActiveConfiguration();
        if (active == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(active);
    }

    @PutMapping("/{configId}")
    @Operation(summary = "Update configuration", description = "Update an existing AI configuration")
    public ResponseEntity<AIConfigResponse> updateConfiguration(
            @PathVariable Long configId,
            @Valid @RequestBody AIConfigRequest request) {
        return ResponseEntity.ok(configService.updateConfiguration(configId, request));
    }

    @PostMapping("/{configId}/activate")
    @Operation(summary = "Activate configuration", description = "Set a configuration as the active provider")
    public ResponseEntity<AIConfigResponse> activateConfiguration(@PathVariable Long configId) {
        return ResponseEntity.ok(configService.activateConfiguration(configId));
    }

    @DeleteMapping("/{configId}")
    @Operation(summary = "Delete configuration", description = "Delete an AI configuration (must not be active)")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable Long configId) {
        configService.deleteConfiguration(configId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{configId}/test")
    @Operation(summary = "Test configuration", description = "Test connectivity with an AI provider")
    public ResponseEntity<AIConfigTestResponse> testConfiguration(@PathVariable Long configId) {
        return ResponseEntity.ok(configService.testConfiguration(configId));
    }
}
