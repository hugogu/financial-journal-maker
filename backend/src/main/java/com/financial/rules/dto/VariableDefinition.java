package com.financial.rules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableDefinition {

    @NotBlank(message = "Variable name is required")
    @Pattern(regexp = "^[a-z][a-z0-9_.]*$", message = "Variable name must start with lowercase letter and contain only lowercase letters, digits, underscores, and dots")
    private String name;

    @NotNull(message = "Variable type is required")
    private ExpressionType type;

    private String currency;

    private String description;
}
