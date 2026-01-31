package com.financial.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIConfigRequest {

    @NotBlank(message = "Provider name is required")
    private String providerName;

    @NotBlank(message = "Model name is required")
    private String modelName;

    private String apiKey;

    private String endpoint;
}
