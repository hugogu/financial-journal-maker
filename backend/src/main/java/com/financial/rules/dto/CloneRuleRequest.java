package com.financial.rules.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloneRuleRequest {

    @NotBlank(message = "New rule code is required")
    @Size(max = 50, message = "Rule code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Rule code must contain only uppercase letters, digits, and hyphens")
    private String newCode;

    @NotBlank(message = "New rule name is required")
    @Size(max = 255, message = "Rule name must not exceed 255 characters")
    private String newName;
}
