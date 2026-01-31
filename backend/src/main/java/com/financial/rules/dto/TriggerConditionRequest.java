package com.financial.rules.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggerConditionRequest {

    @NotNull(message = "Condition JSON is required")
    private Map<String, Object> conditionJson;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
