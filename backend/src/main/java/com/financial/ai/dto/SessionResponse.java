package com.financial.ai.dto;

import com.financial.ai.domain.DesignPhase;
import com.financial.ai.domain.SessionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Long id;
    private String title;
    private SessionStatus status;
    private DesignPhase currentPhase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
