package com.financial.ai.dto;

import com.financial.ai.domain.DesignPhase;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionResponse {

    private Long id;
    private DesignPhase decisionType;
    private String entityType;
    private Map<String, Object> content;
    private Boolean isConfirmed;
    private Long linkedEntityId;
    private LocalDateTime createdAt;
}
