package com.financial.rules.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpressionValidationRequest {

    @NotBlank(message = "Expression is required")
    private String expression;

    @Valid
    private List<VariableDefinition> variableSchema;
}
