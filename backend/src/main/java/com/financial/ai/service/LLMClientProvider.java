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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMClientProvider {

    private final AIConfigRepository configRepository;

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

        OpenAiApi openAiApi = new OpenAiApi(config.getEndpoint(), config.getApiKey());
        
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
}
