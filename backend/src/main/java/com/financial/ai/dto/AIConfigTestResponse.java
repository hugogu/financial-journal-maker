package com.financial.ai.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIConfigTestResponse {

    private Long configId;
    private boolean success;
    private String message;
    private Long responseTime;
}
