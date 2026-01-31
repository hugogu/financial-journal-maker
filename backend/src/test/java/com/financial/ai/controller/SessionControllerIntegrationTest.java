package com.financial.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.dto.DecisionRequest;
import com.financial.ai.dto.SessionCreateRequest;
import com.financial.ai.dto.SessionUpdateRequest;
import com.financial.ai.repository.SessionRepository;
import com.financial.coa.CoaApplication;
import org.junit.jupiter.api.BeforeEach;
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
class SessionControllerIntegrationTest {

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

    @Test
    void createSession_Success() throws Exception {
        SessionCreateRequest request = SessionCreateRequest.builder()
                .title("Test Session")
                .build();

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Session"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currentPhase").value("PRODUCT"));
    }

    @Test
    void createSession_InvalidRequest_ReturnsBadRequest() throws Exception {
        SessionCreateRequest request = SessionCreateRequest.builder()
                .title("")
                .build();

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSession_Success() throws Exception {
        Long sessionId = createTestSession("Test Session");

        mockMvc.perform(get("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId))
                .andExpect(jsonPath("$.title").value("Test Session"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.messageCount").value(0));
    }

    @Test
    void getSession_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SESSION_NOT_FOUND"));
    }

    @Test
    void listSessions_Success() throws Exception {
        createTestSession("Session 1");
        createTestSession("Session 2");

        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void listSessions_FilterByStatus() throws Exception {
        Long sessionId = createTestSession("Active Session");
        createTestSession("Another Session");
        
        mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/sessions")
                        .param("status", "PAUSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("PAUSED"));
    }

    @Test
    void updateSession_Success() throws Exception {
        Long sessionId = createTestSession("Original Title");
        
        SessionUpdateRequest updateRequest = SessionUpdateRequest.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(patch("/api/v1/sessions/{id}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void pauseSession_Success() throws Exception {
        Long sessionId = createTestSession("Test Session");

        mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"));
    }

    @Test
    void resumeSession_Success() throws Exception {
        Long sessionId = createTestSession("Test Session");
        
        mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/sessions/{id}/resume", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void completeSession_Success() throws Exception {
        Long sessionId = createTestSession("Test Session");

        mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void archiveSession_Success() throws Exception {
        Long sessionId = createTestSession("Test Session");
        
        mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/sessions/{id}/archive", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    void archiveSession_InvalidState_ReturnsBadRequest() throws Exception {
        Long sessionId = createTestSession("Test Session");

        mockMvc.perform(post("/api/v1/sessions/{id}/archive", sessionId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
    }

    @Test
    void getMessages_EmptySession() throws Exception {
        Long sessionId = createTestSession("Test Session");

        mockMvc.perform(get("/api/v1/sessions/{id}/messages", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void confirmDecision_Success() throws Exception {
        Long sessionId = createTestSession("Test Session");
        
        Map<String, Object> content = new HashMap<>();
        content.put("name", "Test Product");
        content.put("description", "A test product");
        
        DecisionRequest decisionRequest = DecisionRequest.builder()
                .decisionType(DesignPhase.PRODUCT)
                .entityType("Product")
                .content(content)
                .build();

        mockMvc.perform(post("/api/v1/sessions/{id}/decisions", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.decisionType").value("PRODUCT"))
                .andExpect(jsonPath("$.isConfirmed").value(true));
    }

    @Test
    void getDecisions_Success() throws Exception {
        Long sessionId = createTestSession("Test Session");
        
        Map<String, Object> content = new HashMap<>();
        content.put("name", "Test Product");
        
        DecisionRequest decisionRequest = DecisionRequest.builder()
                .decisionType(DesignPhase.PRODUCT)
                .content(content)
                .build();

        mockMvc.perform(post("/api/v1/sessions/{id}/decisions", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decisionRequest)));

        mockMvc.perform(get("/api/v1/sessions/{id}/decisions", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].decisionType").value("PRODUCT"));
    }

    @Test
    void getDecisions_FilterByConfirmed() throws Exception {
        Long sessionId = createTestSession("Test Session");
        
        Map<String, Object> content = new HashMap<>();
        content.put("name", "Test Product");
        
        DecisionRequest decisionRequest = DecisionRequest.builder()
                .decisionType(DesignPhase.PRODUCT)
                .content(content)
                .isConfirmed(true)
                .build();

        mockMvc.perform(post("/api/v1/sessions/{id}/decisions", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decisionRequest)));

        mockMvc.perform(get("/api/v1/sessions/{id}/decisions", sessionId)
                        .param("confirmed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void maxConcurrentSessions_ReturnsConflict() throws Exception {
        for (int i = 0; i < 5; i++) {
            createTestSession("Session " + i);
        }

        SessionCreateRequest request = SessionCreateRequest.builder()
                .title("Session 6")
                .build();

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("MAX_SESSIONS_EXCEEDED"));
    }

    private Long createTestSession(String title) throws Exception {
        SessionCreateRequest request = SessionCreateRequest.builder()
                .title(title)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
