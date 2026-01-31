package com.financial.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioCloneRequest {

    private Long targetProductId;

    @Size(max = 50, message = "New code must not exceed 50 characters")
    private String newCode;

    @Size(max = 200, message = "New name must not exceed 200 characters")
    private String newName;
}
