package com.financial.rules.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationRequest {

    @NotNull(message = "Event data is required")
    private Map<String, Object> eventData;
}
