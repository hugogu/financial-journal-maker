package com.financial.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CoaApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SessionLifecycleIntegrationTest {

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
    @DisplayName("Pause Transition Tests")
    class PauseTransitionTests {

        @Test
        @DisplayName("Can pause an ACTIVE session")
        void pauseActiveSession_Success() throws Exception {
            Long sessionId = createSession("Test Session");

            mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PAUSED"));
        }

        @Test
        @DisplayName("Cannot pause a PAUSED session")
        void pausePausedSession_Fails() throws Exception {
            Long sessionId = createSession("Test Session");
            pauseSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }

        @Test
        @DisplayName("Cannot pause a COMPLETED session")
        void pauseCompletedSession_Fails() throws Exception {
            Long sessionId = createSession("Test Session");
            completeSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }
    }

    @Nested
    @DisplayName("Resume Transition Tests")
    class ResumeTransitionTests {

        @Test
        @DisplayName("Can resume a PAUSED session")
        void resumePausedSession_Success() throws Exception {
            Long sessionId = createSession("Test Session");
            pauseSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/resume", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Cannot resume an ACTIVE session")
        void resumeActiveSession_Fails() throws Exception {
            Long sessionId = createSession("Test Session");

            mockMvc.perform(post("/api/v1/sessions/{id}/resume", sessionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }

        @Test
        @DisplayName("Cannot resume a COMPLETED session")
        void resumeCompletedSession_Fails() throws Exception {
            Long sessionId = createSession("Test Session");
            completeSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/resume", sessionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }
    }

    @Nested
    @DisplayName("Complete Transition Tests")
    class CompleteTransitionTests {

        @Test
        @DisplayName("Can complete an ACTIVE session")
        void completeActiveSession_Success() throws Exception {
            Long sessionId = createSession("Test Session");

            mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Can complete a PAUSED session")
        void completePausedSession_Success() throws Exception {
            Long sessionId = createSession("Test Session");
            pauseSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Cannot complete an already COMPLETED session")
        void completeCompletedSession_Fails() throws Exception {
            Long sessionId = createSession("Test Session");
            completeSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }
    }

    @Nested
    @DisplayName("Archive Transition Tests")
    class ArchiveTransitionTests {

        @Test
        @DisplayName("Can archive a COMPLETED session")
        void archiveCompletedSession_Success() throws Exception {
            Long sessionId = createSession("Test Session");
            completeSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/archive", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ARCHIVED"));
        }

        @Test
        @DisplayName("Cannot archive an ACTIVE session")
        void archiveActiveSession_Fails() throws Exception {
            Long sessionId = createSession("Test Session");

            mockMvc.perform(post("/api/v1/sessions/{id}/archive", sessionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }

        @Test
        @DisplayName("Cannot archive a PAUSED session")
        void archivePausedSession_Fails() throws Exception {
            Long sessionId = createSession("Test Session");
            pauseSession(sessionId);

            mockMvc.perform(post("/api/v1/sessions/{id}/archive", sessionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }
    }

    @Nested
    @DisplayName("Full Lifecycle Flow Tests")
    class FullLifecycleTests {

        @Test
        @DisplayName("Complete lifecycle: ACTIVE -> PAUSED -> ACTIVE -> COMPLETED -> ARCHIVED")
        void fullLifecycleFlow_Success() throws Exception {
            Long sessionId = createSession("Full Lifecycle Test");

            mockMvc.perform(get("/api/v1/sessions/{id}", sessionId))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                    .andExpect(jsonPath("$.status").value("PAUSED"));

            mockMvc.perform(post("/api/v1/sessions/{id}/resume", sessionId))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            mockMvc.perform(post("/api/v1/sessions/{id}/archive", sessionId))
                    .andExpect(jsonPath("$.status").value("ARCHIVED"));
        }

        @Test
        @DisplayName("Direct path: ACTIVE -> COMPLETED -> ARCHIVED")
        void directCompletionPath_Success() throws Exception {
            Long sessionId = createSession("Direct Path Test");

            mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            mockMvc.perform(post("/api/v1/sessions/{id}/archive", sessionId))
                    .andExpect(jsonPath("$.status").value("ARCHIVED"));
        }
    }

    @Nested
    @DisplayName("Context Preservation Tests")
    class ContextPreservationTests {

        @Test
        @DisplayName("Session data persists across pause/resume")
        void pauseResumePreservesContext() throws Exception {
            Long sessionId = createSession("Context Test");

            mockMvc.perform(patch("/api/v1/sessions/{id}", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"Updated Title\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId));
            mockMvc.perform(post("/api/v1/sessions/{id}/resume", sessionId));

            mockMvc.perform(get("/api/v1/sessions/{id}", sessionId))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Cannot edit COMPLETED session")
        void completedSessionIsReadOnly() throws Exception {
            Long sessionId = createSession("Read-Only Test");
            completeSession(sessionId);

            mockMvc.perform(patch("/api/v1/sessions/{id}", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"Should Fail\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_SESSION_STATE"));
        }
    }

    private Long createSession(String title) throws Exception {
        SessionCreateRequest request = SessionCreateRequest.builder()
                .title(title)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private void pauseSession(Long sessionId) throws Exception {
        mockMvc.perform(post("/api/v1/sessions/{id}/pause", sessionId))
                .andExpect(status().isOk());
    }

    private void completeSession(Long sessionId) throws Exception {
        mockMvc.perform(post("/api/v1/sessions/{id}/complete", sessionId))
                .andExpect(status().isOk());
    }
}
