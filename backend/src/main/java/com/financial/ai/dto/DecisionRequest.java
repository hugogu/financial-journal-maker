package com.financial.ai.dto;

import com.financial.ai.domain.DesignPhase;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionRequest {

    @NotNull(message = "Decision type is required")
    private DesignPhase decisionType;

    private String entityType;

    @NotNull(message = "Content is required")
    private Map<String, Object> content;

    private Boolean isConfirmed = true;

    private Long linkedEntityId;
}
