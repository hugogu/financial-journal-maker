package com.financial.ai.dto;

import com.financial.ai.domain.DesignPhase;
import com.financial.ai.domain.SessionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDetailResponse {

    private Long id;
    private String title;
    private SessionStatus status;
    private DesignPhase currentPhase;
    private Integer messageCount;
    private List<DecisionResponse> confirmedDecisions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
