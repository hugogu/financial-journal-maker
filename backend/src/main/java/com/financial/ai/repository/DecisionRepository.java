package com.financial.ai.repository;

import com.financial.ai.domain.DesignDecision;
import com.financial.ai.domain.DesignPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecisionRepository extends JpaRepository<DesignDecision, Long> {

    List<DesignDecision> findBySessionId(Long sessionId);

    List<DesignDecision> findBySessionIdAndIsConfirmed(Long sessionId, Boolean isConfirmed);

    List<DesignDecision> findBySessionIdAndDecisionType(Long sessionId, DesignPhase decisionType);

    void deleteBySessionId(Long sessionId);
}
