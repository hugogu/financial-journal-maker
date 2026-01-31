package com.financial.ai.service;

import com.financial.ai.domain.AIConfiguration;
import com.financial.ai.dto.AIConfigRequest;
import com.financial.ai.dto.AIConfigResponse;
import com.financial.ai.dto.AIConfigTestResponse;
import com.financial.ai.repository.AIConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIConfigService {

    private final AIConfigRepository configRepository;
    
    private static final String ENCRYPTION_KEY = "FinancialAI16Key";

    @Transactional
    public AIConfigResponse createConfiguration(AIConfigRequest request) {
        String endpoint = request.getEndpoint() != null ? request.getEndpoint() : getDefaultEndpoint(request.getProviderName());
        AIConfiguration config = AIConfiguration.builder()
                .providerName(request.getProviderName())
                .displayName(request.getProviderName() + " - " + request.getModelName())
                .modelName(request.getModelName())
                .apiKey(encryptApiKey(request.getApiKey()))
                .endpoint(endpoint)
                .isActive(false)
                .priority(0)
                .build();

        config = configRepository.save(config);
        log.info("Created AI configuration for provider: {}", request.getProviderName());

        return toResponse(config);
    }
    
    private String getDefaultEndpoint(String providerName) {
        return switch (providerName.toLowerCase()) {
            case "openai" -> "https://api.openai.com";
            case "anthropic" -> "https://api.anthropic.com";
            case "azure" -> "https://api.openai.azure.com";
            default -> "https://api.openai.com";
        };
    }

    @Transactional(readOnly = true)
    public List<AIConfigResponse> getAllConfigurations() {
        return configRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AIConfigResponse getConfiguration(Long configId) {
        AIConfiguration config = configRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));
        return toResponse(config);
    }

    @Transactional(readOnly = true)
    public AIConfigResponse getActiveConfiguration() {
        return configRepository.findByIsActiveTrue()
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public AIConfigResponse updateConfiguration(Long configId, AIConfigRequest request) {
        AIConfiguration config = configRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));

        if (request.getProviderName() != null) {
            config.setProviderName(request.getProviderName());
        }
        if (request.getModelName() != null) {
            config.setModelName(request.getModelName());
        }
        // Only update API key if provided and not empty
        if (request.getApiKey() != null && !request.getApiKey().trim().isEmpty()) {
            config.setApiKey(encryptApiKey(request.getApiKey()));
        }
        if (request.getEndpoint() != null) {
            config.setEndpoint(request.getEndpoint());
        }

        config = configRepository.save(config);
        log.info("Updated AI configuration: {}", configId);

        return toResponse(config);
    }

    @Transactional
    public AIConfigResponse activateConfiguration(Long configId) {
        configRepository.findByIsActiveTrue()
                .ifPresent(current -> {
                    current.setIsActive(false);
                    configRepository.save(current);
                });

        AIConfiguration config = configRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));

        config.setIsActive(true);
        config = configRepository.save(config);
        log.info("Activated AI configuration: {} ({})", configId, config.getProviderName());

        return toResponse(config);
    }

    @Transactional
    public void deleteConfiguration(Long configId) {
        AIConfiguration config = configRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));

        if (config.getIsActive()) {
            throw new IllegalStateException("Cannot delete active configuration. Deactivate first.");
        }

        configRepository.delete(config);
        log.info("Deleted AI configuration: {}", configId);
    }

    public AIConfigTestResponse testConfiguration(Long configId) {
        AIConfiguration config = configRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Configuration not found: " + configId));

        try {
            String decryptedKey = decryptApiKey(config.getApiKey());
            
            // Log API key presence for debugging
            log.info("Testing configuration {} - Provider: {}, Model: {}, Endpoint: {}, API Key present: {}, API Key length: {}", 
                    configId, config.getProviderName(), config.getModelName(), config.getEndpoint(),
                    decryptedKey != null && !decryptedKey.isEmpty(), 
                    decryptedKey != null ? decryptedKey.length() : 0);
            
            if (decryptedKey == null || decryptedKey.trim().isEmpty()) {
                log.error("API key is null or empty for configuration {}", configId);
                throw new IllegalStateException("API key is not configured");
            }
            
            // Log full details for debugging
            log.debug("TEST REQUEST - Endpoint: {}, Model: {}, API Key (first 10 chars): {}", 
                    config.getEndpoint(), config.getModelName(), 
                    decryptedKey.substring(0, Math.min(10, decryptedKey.length())));
            
            OpenAiApi api = new OpenAiApi(config.getEndpoint(), decryptedKey);
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .withModel(config.getModelName())
                    .build();
            OpenAiChatModel chatModel = new OpenAiChatModel(api, options);

            log.debug("TEST REQUEST - Calling chatModel.call()");
            String response = chatModel.call("Hello, respond with 'OK' if you can read this.");
            log.debug("TEST REQUEST - Response received: {}", response != null ? response.substring(0, Math.min(50, response.length())) : "null");

            boolean success = response != null && !response.isEmpty();
            return AIConfigTestResponse.builder()
                    .configId(configId)
                    .success(success)
                    .message(success ? "Connection successful" : "No response received")
                    .responseTime(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Configuration test failed for {}: {}", configId, e.getMessage());
            return AIConfigTestResponse.builder()
                    .configId(configId)
                    .success(false)
                    .message("Connection failed: " + e.getMessage())
                    .build();
        }
    }

    private String encryptApiKey(String apiKey) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt API key", e);
        }
    }

    private String decryptApiKey(String encryptedKey) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedKey));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt API key", e);
        }
    }

    private AIConfigResponse toResponse(AIConfiguration config) {
        return AIConfigResponse.builder()
                .id(config.getId())
                .providerName(config.getProviderName())
                .modelName(config.getModelName())
                .endpoint(config.getEndpoint())
                .isActive(config.getIsActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
