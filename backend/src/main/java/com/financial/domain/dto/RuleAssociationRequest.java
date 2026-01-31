package com.financial.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleAssociationRequest {

    @NotNull(message = "Rule ID is required")
    private Long ruleId;

    private Integer sequenceNumber;
}
