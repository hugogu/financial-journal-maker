package com.financial.rules.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleCreateRequest {

    @NotBlank(message = "Rule code is required")
    @Size(max = 50, message = "Rule code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Rule code must contain only uppercase letters, digits, and hyphens")
    private String code;

    @NotBlank(message = "Rule name is required")
    @Size(max = 255, message = "Rule name must not exceed 255 characters")
    private String name;

    private String description;

    @Builder.Default
    private Boolean sharedAcrossScenarios = false;

    @NotNull(message = "Entry template is required")
    @Valid
    private EntryTemplateRequest entryTemplate;

    @Valid
    private List<TriggerConditionRequest> triggerConditions;
}
