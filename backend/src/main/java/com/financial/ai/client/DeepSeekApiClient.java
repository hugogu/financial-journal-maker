package com.financial.ai.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Custom API client for DeepSeek that uses the correct endpoint structure.
 * DeepSeek uses /chat/completions instead of /v1/chat/completions
 */
@Slf4j
public class DeepSeekApiClient {
    
    private final WebClient webClient;
    private final String model;
    
    public DeepSeekApiClient(String baseUrl, String apiKey, String model) {
        this.model = model;
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        
        log.info("Created DeepSeekApiClient with baseUrl: {}, model: {}", baseUrl, model);
    }
    
    public String chat(String message) {
        Map<String, Object> request = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "user", "content", message)
            ),
            "stream", false
        );
        
        Map<String, Object> response = webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .block();
        
        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message1 = (Map<String, Object>) choices.get(0).get("message");
                return (String) message1.get("content");
            }
        }
        
        return null;
    }
    
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        Map<String, Object> request = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ),
            "stream", true
        );
        
        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String.class)
            .map(this::extractContent);
    }
    
    private String extractContent(String line) {
        // Parse SSE format: data: {...}
        if (line.startsWith("data: ")) {
            String json = line.substring(6);
            if ("[DONE]".equals(json)) {
                return "";
            }
            // Simple extraction - in production, use proper JSON parsing
            // This is a simplified version
            return json;
        }
        return "";
    }
}
