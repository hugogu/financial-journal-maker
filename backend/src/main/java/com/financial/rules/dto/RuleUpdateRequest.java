package com.financial.rules.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
public class RuleUpdateRequest {

    @Size(max = 255, message = "Rule name must not exceed 255 characters")
    private String name;

    private String description;

    private Boolean sharedAcrossScenarios;

    @Valid
    private EntryTemplateRequest entryTemplate;

    @Valid
    private List<TriggerConditionRequest> triggerConditions;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}
