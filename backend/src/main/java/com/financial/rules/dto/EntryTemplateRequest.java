package com.financial.rules.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryTemplateRequest {

    private String description;

    @Valid
    private List<VariableDefinition> variableSchema;

    @NotEmpty(message = "At least one entry line is required")
    @Valid
    private List<EntryLineRequest> lines;
}
