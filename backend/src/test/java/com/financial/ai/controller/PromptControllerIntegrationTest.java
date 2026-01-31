package com.financial.ai.controller;

import com.financial.ai.domain.DesignPhase;
import com.financial.ai.domain.PromptTemplate;
import com.financial.ai.dto.PromptRequest;
import com.financial.ai.repository.PromptRepository;
import com.financial.coa.CoaApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CoaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PromptControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        promptRepository.deleteAll();
    }

    @Test
    void createPrompt_Success() throws Exception {
        PromptRequest request = PromptRequest.builder()
                .name("Test Prompt")
                .designPhase(DesignPhase.PRODUCT)
                .content("Test prompt content with {{userMessage}}")
                .build();

        mockMvc.perform(post("/admin/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Prompt"))
                .andExpect(jsonPath("$.designPhase").value("PRODUCT"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void createPrompt_VersionIncrement() throws Exception {
        // Create first version
        PromptRequest request1 = PromptRequest.builder()
                .name("Version Test")
                .designPhase(DesignPhase.SCENARIO)
                .content("Version 1 content")
                .build();

        mockMvc.perform(post("/admin/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(1));

        // Create second version with same name
        PromptRequest request2 = PromptRequest.builder()
                .name("Version Test")
                .designPhase(DesignPhase.SCENARIO)
                .content("Version 2 content")
                .build();

        mockMvc.perform(post("/admin/prompts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void getAllPrompts_Success() throws Exception {
        createTestPrompt("Prompt 1", DesignPhase.PRODUCT, "Content 1");
        createTestPrompt("Prompt 2", DesignPhase.SCENARIO, "Content 2");

        mockMvc.perform(get("/admin/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getPromptsByPhase_Success() throws Exception {
        createTestPrompt("Product Prompt", DesignPhase.PRODUCT, "Content");
        createTestPrompt("Scenario Prompt", DesignPhase.SCENARIO, "Content");

        mockMvc.perform(get("/admin/prompts/phase/PRODUCT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Product Prompt"));
    }

    @Test
    void activatePrompt_Success() throws Exception {
        PromptTemplate prompt = createTestPrompt("Activate Test", DesignPhase.PRODUCT, "Content");

        mockMvc.perform(post("/admin/prompts/" + prompt.getId() + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        // Verify via get
        mockMvc.perform(get("/admin/prompts/phase/PRODUCT/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prompt.getId()));
    }

    @Test
    void activatePrompt_DeactivatesPrevious() throws Exception {
        PromptTemplate prompt1 = createTestPrompt("First", DesignPhase.PRODUCT, "Content 1");
        prompt1.setIsActive(true);
        promptRepository.save(prompt1);

        PromptTemplate prompt2 = createTestPrompt("Second", DesignPhase.PRODUCT, "Content 2");

        // Activate prompt2
        mockMvc.perform(post("/admin/prompts/" + prompt2.getId() + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        // Verify prompt1 is deactivated
        mockMvc.perform(get("/admin/prompts/" + prompt1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void updatePrompt_CreatesNewVersion() throws Exception {
        PromptTemplate original = createTestPrompt("Update Test", DesignPhase.PRODUCT, "Original content");

        PromptRequest updateRequest = PromptRequest.builder()
                .name("Update Test")
                .designPhase(DesignPhase.PRODUCT)
                .content("Updated content")
                .build();

        mockMvc.perform(put("/admin/prompts/" + original.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void rollbackToVersion_Success() throws Exception {
        // Create multiple versions
        createTestPrompt("Rollback Test", DesignPhase.PRODUCT, "Version 1");
        createTestPrompt("Rollback Test", DesignPhase.PRODUCT, "Version 2");
        createTestPrompt("Rollback Test", DesignPhase.PRODUCT, "Version 3");

        // Rollback to version 1
        mockMvc.perform(post("/admin/prompts/rollback")
                        .param("name", "Rollback Test")
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(4))
                .andExpect(jsonPath("$.content").value("Version 1"));
    }

    @Test
    void getVersionHistory_Success() throws Exception {
        createTestPrompt("History Test", DesignPhase.PRODUCT, "V1");
        createTestPrompt("History Test", DesignPhase.PRODUCT, "V2");
        createTestPrompt("History Test", DesignPhase.PRODUCT, "V3");

        mockMvc.perform(get("/admin/prompts/history/History Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].version").value(3)) // Descending order
                .andExpect(jsonPath("$[1].version").value(2))
                .andExpect(jsonPath("$[2].version").value(1));
    }

    @Test
    void deletePrompt_Success() throws Exception {
        PromptTemplate prompt = createTestPrompt("Delete Test", DesignPhase.PRODUCT, "Content");

        mockMvc.perform(delete("/admin/prompts/" + prompt.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/admin/prompts/" + prompt.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deletePrompt_FailsIfActive() throws Exception {
        PromptTemplate prompt = createTestPrompt("Active Delete Test", DesignPhase.PRODUCT, "Content");
        prompt.setIsActive(true);
        promptRepository.save(prompt);

        mockMvc.perform(delete("/admin/prompts/" + prompt.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void initializeDefaults_Success() throws Exception {
        mockMvc.perform(post("/admin/prompts/initialize"))
                .andExpect(status().isOk());

        // Should have created prompts for all phases
        mockMvc.perform(get("/admin/prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }

    private PromptTemplate createTestPrompt(String name, DesignPhase phase, String content) {
        int version = promptRepository.findByNameOrderByVersionDesc(name)
                .stream()
                .findFirst()
                .map(p -> p.getVersion() + 1)
                .orElse(1);

        PromptTemplate prompt = PromptTemplate.builder()
                .name(name)
                .designPhase(phase)
                .content(content)
                .version(version)
                .isActive(false)
                .build();
        return promptRepository.save(prompt);
    }
}
