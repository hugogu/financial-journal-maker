package com.financial.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.dto.DecisionRequest;
import com.financial.ai.dto.SessionCreateRequest;
import com.financial.ai.repository.SessionRepository;
import com.financial.coa.CoaApplication;
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CoaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
    }

    @Nested
    @DisplayName("Export Validation Tests")
    class ExportValidationTests {

        @Test
        @DisplayName("Cannot export from non-completed session")
        void exportFromActiveSession_Fails() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(false);

            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", sessionId, "COA"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }

        @Test
        @DisplayName("Cannot export from non-existent session")
        void exportFromNonExistentSession_Fails() throws Exception {
            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", 99999, "COA"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SESSION_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("COA Export Tests")
    class CoaExportTests {

        @Test
        @DisplayName("Export COA successfully")
        void exportCoa_Success() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(true);

            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", sessionId, "COA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value(sessionId))
                    .andExpect(jsonPath("$.exportType").value("COA"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.content").isNotEmpty())
                    .andExpect(jsonPath("$.artifactId").isNumber());
        }

        @Test
        @DisplayName("COA export content contains account information")
        void exportCoa_ContainsAccountInfo() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(true);

            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", sessionId, "COA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", containsString("Chart of Accounts")));
        }
    }

    @Nested
    @DisplayName("Rules Export Tests")
    class RulesExportTests {

        @Test
        @DisplayName("Export rules successfully")
        void exportRules_Success() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(true);

            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", sessionId, "RULES"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exportType").value("RULES"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.content", containsString("Accounting Rules")));
        }
    }

    @Nested
    @DisplayName("Numscript Export Tests")
    class NumscriptExportTests {

        @Test
        @DisplayName("Export Numscript successfully")
        void exportNumscript_Success() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(true);

            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", sessionId, "NUMSCRIPT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exportType").value("NUMSCRIPT"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.content", containsString("Numscript")));
        }
    }

    @Nested
    @DisplayName("Export History Tests")
    class ExportHistoryTests {

        @Test
        @DisplayName("Get export history returns all exports")
        void getExportHistory_Success() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(true);

            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", sessionId, "COA"));
            mockMvc.perform(post("/api/v1/sessions/{id}/export/{type}", sessionId, "RULES"));

            mockMvc.perform(get("/api/v1/sessions/{id}/export", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Empty export history for new session")
        void getExportHistory_EmptyForNewSession() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(true);

            mockMvc.perform(get("/api/v1/sessions/{id}/export", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Conflict Detection Tests")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Preview conflicts returns conflict info")
        void previewConflicts_Success() throws Exception {
            Long sessionId = createAndCompleteSessionWithDecisions(true);

            mockMvc.perform(get("/api/v1/sessions/{id}/export/{type}/conflicts", sessionId, "COA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exportType").value("COA"))
                    .andExpect(jsonPath("$.hasConflicts").isBoolean());
        }
    }

    private Long createAndCompleteSessionWithDecisions(boolean complete) throws Exception {
        SessionCreateRequest createRequest = SessionCreateRequest.builder()
                .title("Export Test Session")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        Map<String, Object> content = new HashMap<>();
        content.put("name", "Test Account");
        content.put("type", "ASSET");
        content.put("code", "1001");
        content.put("description", "Test account for export");

        DecisionRequest decisionRequest = DecisionRequest.builder()
                .decisionType(DesignPhase.ACCOUNTING)
                .entityType("Account")
                .content(content)
                .build();

        mockMvc.perform(post("/api/v1/sessions/{id}/decisions", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionRequest)))
                .andExpect(status().isOk());

        if (complete) {
            mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                    .andExpect(status().isOk());
        }

        return sessionId;
    }
}
