package com.financial.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.ai.dto.AIConfigRequest;
import com.financial.ai.repository.AIConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AIConfigControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AIConfigRepository configRepository;

    @BeforeEach
    void setUp() {
        configRepository.deleteAll();
    }

    @Nested
    @DisplayName("Configuration CRUD Tests")
    class CrudTests {

        @Test
        @DisplayName("Create configuration successfully")
        void createConfiguration_Success() throws Exception {
            AIConfigRequest request = AIConfigRequest.builder()
                    .providerName("openai")
                    .modelName("gpt-4")
                    .apiKey("sk-test-key")
                    .endpoint("https://api.openai.com/v1")
                    .build();

            mockMvc.perform(post("/api/v1/admin/ai-config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.providerName").value("openai"))
                    .andExpect(jsonPath("$.modelName").value("gpt-4"))
                    .andExpect(jsonPath("$.isActive").value(false));
        }

        @Test
        @DisplayName("List all configurations")
        void listConfigurations_Success() throws Exception {
            createConfig("openai", "gpt-4");
            createConfig("anthropic", "claude-3");

            mockMvc.perform(get("/api/v1/admin/ai-config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Get configuration by ID")
        void getConfiguration_Success() throws Exception {
            Long configId = createConfig("openai", "gpt-4");

            mockMvc.perform(get("/api/v1/admin/ai-config/{id}", configId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(configId))
                    .andExpect(jsonPath("$.providerName").value("openai"));
        }

        @Test
        @DisplayName("Update configuration")
        void updateConfiguration_Success() throws Exception {
            Long configId = createConfig("openai", "gpt-4");

            AIConfigRequest updateRequest = AIConfigRequest.builder()
                    .providerName("openai")
                    .modelName("gpt-4-turbo")
                    .build();

            mockMvc.perform(put("/api/v1/admin/ai-config/{id}", configId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.modelName").value("gpt-4-turbo"));
        }

        @Test
        @DisplayName("Delete configuration")
        void deleteConfiguration_Success() throws Exception {
            Long configId = createConfig("openai", "gpt-4");

            mockMvc.perform(delete("/api/v1/admin/ai-config/{id}", configId))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/admin/ai-config/{id}", configId))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Activation Tests")
    class ActivationTests {

        @Test
        @DisplayName("Activate configuration")
        void activateConfiguration_Success() throws Exception {
            Long configId = createConfig("openai", "gpt-4");

            mockMvc.perform(post("/api/v1/admin/ai-config/{id}/activate", configId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("Get active configuration")
        void getActiveConfiguration_Success() throws Exception {
            Long configId = createConfig("openai", "gpt-4");
            activateConfig(configId);

            mockMvc.perform(get("/api/v1/admin/ai-config/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(configId))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("No active configuration returns empty")
        void getActiveConfiguration_NoActive() throws Exception {
            createConfig("openai", "gpt-4");

            mockMvc.perform(get("/api/v1/admin/ai-config/active"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Activating new config deactivates previous")
        void activateConfiguration_DeactivatesPrevious() throws Exception {
            Long config1 = createConfig("openai", "gpt-4");
            Long config2 = createConfig("anthropic", "claude-3");

            activateConfig(config1);
            activateConfig(config2);

            mockMvc.perform(get("/api/v1/admin/ai-config/{id}", config1))
                    .andExpect(jsonPath("$.isActive").value(false));

            mockMvc.perform(get("/api/v1/admin/ai-config/{id}", config2))
                    .andExpect(jsonPath("$.isActive").value(true));
        }

        @Test
        @DisplayName("Cannot delete active configuration")
        void deleteActiveConfiguration_Fails() throws Exception {
            Long configId = createConfig("openai", "gpt-4");
            activateConfig(configId);

            mockMvc.perform(delete("/api/v1/admin/ai-config/{id}", configId))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("Test Connectivity Tests")
    class ConnectivityTests {

        @Test
        @DisplayName("Test configuration returns result")
        void testConfiguration_ReturnsResult() throws Exception {
            Long configId = createConfig("openai", "gpt-4");

            mockMvc.perform(post("/api/v1/admin/ai-config/{id}/test", configId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.configId").value(configId))
                    .andExpect(jsonPath("$.success").isBoolean())
                    .andExpect(jsonPath("$.message").isString());
        }
    }

    private Long createConfig(String provider, String model) throws Exception {
        AIConfigRequest request = AIConfigRequest.builder()
                .providerName(provider)
                .modelName(model)
                .apiKey("test-key-" + provider)
                .endpoint("https://api." + provider + ".com/v1")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/admin/ai-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private void activateConfig(Long configId) throws Exception {
        mockMvc.perform(post("/api/v1/admin/ai-config/{id}/activate", configId))
                .andExpect(status().isOk());
    }
}
