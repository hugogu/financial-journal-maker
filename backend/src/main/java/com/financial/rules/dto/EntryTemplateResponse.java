package com.financial.rules.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.rules.domain.EntryTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class EntryTemplateResponse {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Long id;
    private String description;
    private List<VariableDefinition> variableSchema;
    private List<EntryLineResponse> lines;

    public static EntryTemplateResponse fromEntity(EntryTemplate template) {
        List<VariableDefinition> schema = new ArrayList<>();
        try {
            if (template.getVariableSchemaJson() != null && !template.getVariableSchemaJson().isBlank()) {
                schema = objectMapper.readValue(template.getVariableSchemaJson(), 
                        new TypeReference<List<VariableDefinition>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse variable schema JSON: {}", e.getMessage());
        }

        List<EntryLineResponse> lineResponses = template.getLines() != null 
                ? template.getLines().stream().map(EntryLineResponse::fromEntity).toList()
                : new ArrayList<>();

        return EntryTemplateResponse.builder()
                .id(template.getId())
                .description(template.getDescription())
                .variableSchema(schema)
                .lines(lineResponses)
                .build();
    }
}
