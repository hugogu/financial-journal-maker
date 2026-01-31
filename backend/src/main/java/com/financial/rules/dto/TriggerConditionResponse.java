package com.financial.rules.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.rules.domain.TriggerCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class TriggerConditionResponse {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Long id;
    private Map<String, Object> conditionJson;
    private String description;
    private String humanReadable;

    public static TriggerConditionResponse fromEntity(TriggerCondition condition) {
        Map<String, Object> json = new HashMap<>();
        try {
            if (condition.getConditionJson() != null && !condition.getConditionJson().isBlank()) {
                json = objectMapper.readValue(condition.getConditionJson(), 
                        new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse condition JSON: {}", e.getMessage());
        }

        return TriggerConditionResponse.builder()
                .id(condition.getId())
                .conditionJson(json)
                .description(condition.getDescription())
                .humanReadable(generateHumanReadable(json))
                .build();
    }

    private static String generateHumanReadable(Map<String, Object> json) {
        if (json == null || json.isEmpty()) {
            return "No condition";
        }
        
        String type = (String) json.get("type");
        if ("SIMPLE".equals(type)) {
            String field = (String) json.get("field");
            String operator = (String) json.get("operator");
            Object value = json.get("value");
            return String.format("%s %s %s", field, operatorToSymbol(operator), value);
        } else if ("AND".equals(type) || "OR".equals(type)) {
            return type + " condition group";
        }
        
        return json.toString();
    }

    private static String operatorToSymbol(String operator) {
        if (operator == null) return "?";
        return switch (operator) {
            case "EQUALS" -> "=";
            case "NOT_EQUALS" -> "!=";
            case "GREATER_THAN" -> ">";
            case "GREATER_THAN_OR_EQUALS" -> ">=";
            case "LESS_THAN" -> "<";
            case "LESS_THAN_OR_EQUALS" -> "<=";
            case "CONTAINS" -> "contains";
            case "MATCHES" -> "matches";
            case "IN" -> "in";
            case "NOT_IN" -> "not in";
            default -> operator;
        };
    }
}
