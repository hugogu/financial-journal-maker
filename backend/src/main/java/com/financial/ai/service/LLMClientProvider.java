package com.financial.ai.service;

import com.financial.ai.domain.AIConfiguration;
import com.financial.ai.repository.AIConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMClientProvider {

    private final AIConfigRepository configRepository;
    
    private static final String ENCRYPTION_KEY = "FinancialAI16Key";

    public Optional<ChatClient> getActiveChatClient() {
        return configRepository.findByIsActiveTrue()
                .map(this::createChatClient);
    }

    public ChatClient getChatClientOrThrow() {
        return getActiveChatClient()
                .orElseThrow(() -> new IllegalStateException("No active AI configuration found"));
    }

    private ChatClient createChatClient(AIConfiguration config) {
        log.info("Creating ChatClient for provider: {} with model: {}", 
                config.getProviderName(), config.getModelName());

        // CRITICAL: Decrypt the API key from database
        String apiKey = decryptApiKey(config.getApiKey());
        
        // Log API key presence for debugging
        log.info("ChatClient configuration - Provider: {}, Model: {}, Endpoint: {}, API Key present: {}, API Key length: {}", 
                config.getProviderName(), config.getModelName(), config.getEndpoint(),
                apiKey != null && !apiKey.isEmpty(), 
                apiKey != null ? apiKey.length() : 0);
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("API key is null or empty for active configuration: {}", config.getId());
            throw new IllegalStateException("API key is not configured for active AI provider");
        }

        // Log full details for debugging
        log.debug("STREAMING REQUEST - Endpoint: {}, Model: {}, API Key (first 10 chars): {}", 
                config.getEndpoint(), config.getModelName(), 
                apiKey.substring(0, Math.min(10, apiKey.length())));

        OpenAiApi openAiApi = new OpenAiApi(config.getEndpoint(), apiKey);
        log.info("Created OpenAiApi with endpoint: {}", config.getEndpoint());
        
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(config.getModelName())
                .withTemperature(0.7)
                .build();

        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, options);
        
        return ChatClient.builder(chatModel).build();
    }

    public boolean hasActiveConfiguration() {
        return configRepository.findByIsActiveTrue().isPresent();
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
}
