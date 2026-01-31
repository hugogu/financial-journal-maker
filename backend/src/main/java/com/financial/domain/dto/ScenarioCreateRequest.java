package com.financial.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioCreateRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Scenario code is required")
    @Size(max = 50, message = "Scenario code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Scenario name is required")
    @Size(max = 200, message = "Scenario name must not exceed 200 characters")
    private String name;

    private String description;

    private String triggerDescription;

    private String fundFlowPath;
}
