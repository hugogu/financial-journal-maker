package com.financial.ai.service;

import com.financial.ai.domain.DesignDecision;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.dto.DecisionRequest;
import com.financial.ai.dto.DecisionResponse;
import com.financial.ai.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionService {

    private final DecisionRepository decisionRepository;

    @Transactional
    public DecisionResponse createOrUpdateDecision(Long sessionId, DecisionRequest request) {
        DesignDecision decision = DesignDecision.builder()
                .sessionId(sessionId)
                .decisionType(request.getDecisionType())
                .entityType(request.getEntityType())
                .content(request.getContent())
                .isConfirmed(request.getIsConfirmed() != null ? request.getIsConfirmed() : true)
                .linkedEntityId(request.getLinkedEntityId())
                .build();

        decision = decisionRepository.save(decision);
        log.info("Created decision {} for session {} at phase {}", 
                decision.getId(), sessionId, request.getDecisionType());

        return toResponse(decision);
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getDecisions(Long sessionId, Boolean confirmed) {
        List<DesignDecision> decisions;
        if (confirmed != null) {
            decisions = decisionRepository.findBySessionIdAndIsConfirmed(sessionId, confirmed);
        } else {
            decisions = decisionRepository.findBySessionId(sessionId);
        }
        return decisions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getDecisionsByPhase(Long sessionId, DesignPhase phase) {
        return decisionRepository.findBySessionIdAndDecisionType(sessionId, phase)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getConfirmedDecisions(Long sessionId) {
        return decisionRepository.findBySessionIdAndIsConfirmed(sessionId, true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DecisionResponse toResponse(DesignDecision decision) {
        return DecisionResponse.builder()
                .id(decision.getId())
                .decisionType(decision.getDecisionType())
                .entityType(decision.getEntityType())
                .content(decision.getContent())
                .isConfirmed(decision.getIsConfirmed())
                .linkedEntityId(decision.getLinkedEntityId())
                .createdAt(decision.getCreatedAt())
                .build();
    }
}
