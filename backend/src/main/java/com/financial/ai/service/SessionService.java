package com.financial.ai.service;

import com.financial.ai.domain.AnalysisSession;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.domain.SessionStatus;
import com.financial.ai.dto.*;
import com.financial.ai.exception.InvalidSessionStateException;
import com.financial.ai.exception.MaxSessionsExceededException;
import com.financial.ai.exception.SessionNotFoundException;
import com.financial.ai.repository.DecisionRepository;
import com.financial.ai.repository.MessageRepository;
import com.financial.ai.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final DecisionRepository decisionRepository;

    @Value("${ai.session.max-concurrent:5}")
    private int maxConcurrentSessions;

    private static final String DEFAULT_ANALYST_ID = "default-analyst";

    @Transactional
    public SessionResponse createSession(SessionCreateRequest request) {
        String analystId = DEFAULT_ANALYST_ID;
        
        validateConcurrentSessionLimit(analystId);

        AnalysisSession session = AnalysisSession.builder()
                .title(request.getTitle())
                .status(SessionStatus.ACTIVE)
                .currentPhase(DesignPhase.PRODUCT)
                .analystId(analystId)
                .build();

        session = sessionRepository.save(session);
        log.info("Created session {} for analyst {}", session.getId(), analystId);

        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(Long sessionId) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetail(Long sessionId) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        
        int messageCount = (int) messageRepository.countBySessionId(sessionId);
        List<DecisionResponse> confirmedDecisions = decisionRepository
                .findBySessionIdAndIsConfirmed(sessionId, true)
                .stream()
                .map(d -> DecisionResponse.builder()
                        .id(d.getId())
                        .decisionType(d.getDecisionType())
                        .entityType(d.getEntityType())
                        .content(d.getContent())
                        .isConfirmed(d.getIsConfirmed())
                        .linkedEntityId(d.getLinkedEntityId())
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SessionDetailResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .messageCount(messageCount)
                .confirmedDecisions(confirmedDecisions)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<SessionResponse> listSessions(SessionStatus status, Pageable pageable) {
        Page<AnalysisSession> sessions;
        if (status != null) {
            sessions = sessionRepository.findByStatus(status, pageable);
        } else {
            sessions = sessionRepository.findAll(pageable);
        }
        return sessions.map(this::toResponse);
    }

    @Transactional
    public SessionResponse updateSession(Long sessionId, SessionUpdateRequest request) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        
        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.ARCHIVED) {
            throw new InvalidSessionStateException(session.getStatus(), "update");
        }

        if (request.getTitle() != null) {
            session.setTitle(request.getTitle());
        }

        session = sessionRepository.save(session);
        log.info("Updated session {}", sessionId);

        return toResponse(session);
    }

    @Transactional
    public SessionResponse updatePhase(Long sessionId, DesignPhase newPhase) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionStateException(session.getStatus(), "change phase");
        }

        session.setCurrentPhase(newPhase);
        session = sessionRepository.save(session);
        log.info("Session {} phase updated to {}", sessionId, newPhase);

        return toResponse(session);
    }

    @Transactional
    public SessionResponse pauseSession(Long sessionId) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionStateException(session.getStatus(), "pause");
        }

        session.setStatus(SessionStatus.PAUSED);
        session = sessionRepository.save(session);
        log.info("Session {} paused", sessionId);

        return toResponse(session);
    }

    @Transactional
    public SessionResponse resumeSession(Long sessionId) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        
        if (session.getStatus() != SessionStatus.PAUSED) {
            throw new InvalidSessionStateException(session.getStatus(), "resume");
        }

        String analystId = session.getAnalystId();
        validateConcurrentSessionLimit(analystId);

        session.setStatus(SessionStatus.ACTIVE);
        session = sessionRepository.save(session);
        log.info("Session {} resumed", sessionId);

        return toResponse(session);
    }

    @Transactional
    public SessionResponse completeSession(Long sessionId) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        
        if (session.getStatus() != SessionStatus.ACTIVE && session.getStatus() != SessionStatus.PAUSED) {
            throw new InvalidSessionStateException(session.getStatus(), "complete");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session = sessionRepository.save(session);
        log.info("Session {} completed", sessionId);

        return toResponse(session);
    }

    @Transactional
    public SessionResponse archiveSession(Long sessionId) {
        AnalysisSession session = findSessionOrThrow(sessionId);
        
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new InvalidSessionStateException(session.getStatus(), "archive");
        }

        session.setStatus(SessionStatus.ARCHIVED);
        session = sessionRepository.save(session);
        log.info("Session {} archived", sessionId);

        return toResponse(session);
    }

    private void validateConcurrentSessionLimit(String analystId) {
        List<SessionStatus> activeStatuses = Arrays.asList(SessionStatus.ACTIVE, SessionStatus.PAUSED);
        long activeCount = sessionRepository.countByAnalystIdAndStatusIn(analystId, activeStatuses);
        
        if (activeCount >= maxConcurrentSessions) {
            throw new MaxSessionsExceededException(analystId, maxConcurrentSessions);
        }
    }

    private AnalysisSession findSessionOrThrow(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    private SessionResponse toResponse(AnalysisSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
