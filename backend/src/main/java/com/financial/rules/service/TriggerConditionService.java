package com.financial.rules.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.rules.domain.TriggerCondition;
import com.financial.rules.dto.TriggerConditionRequest;
import com.financial.rules.dto.TriggerConditionResponse;
import com.financial.rules.exception.RulesException;
import com.financial.rules.repository.TriggerConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TriggerConditionService {

    private final TriggerConditionRepository conditionRepository;
    private final ObjectMapper objectMapper;

    public List<TriggerConditionResponse> getConditions(Long ruleId) {
        List<TriggerCondition> conditions = conditionRepository.findByRuleId(ruleId);
        return conditions.stream()
                .map(TriggerConditionResponse::fromEntity)
                .toList();
    }

    public List<TriggerCondition> saveConditions(Long ruleId, List<TriggerConditionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<TriggerCondition> conditions = new ArrayList<>();
        for (TriggerConditionRequest request : requests) {
            TriggerCondition condition = createCondition(ruleId, request);
            conditions.add(conditionRepository.save(condition));
        }
        
        log.info("Saved {} trigger conditions for rule {}", conditions.size(), ruleId);
        return conditions;
    }

    public void deleteConditions(Long ruleId) {
        conditionRepository.deleteByRuleId(ruleId);
        log.info("Deleted trigger conditions for rule {}", ruleId);
    }

    public List<TriggerCondition> cloneConditions(Long sourceRuleId, Long targetRuleId) {
        List<TriggerCondition> sourceConditions = conditionRepository.findByRuleId(sourceRuleId);
        if (sourceConditions.isEmpty()) {
            return new ArrayList<>();
        }

        List<TriggerCondition> clonedConditions = new ArrayList<>();
        for (TriggerCondition source : sourceConditions) {
            TriggerCondition cloned = TriggerCondition.builder()
                    .ruleId(targetRuleId)
                    .conditionJson(source.getConditionJson())
                    .description(source.getDescription())
                    .build();
            clonedConditions.add(conditionRepository.save(cloned));
        }

        log.info("Cloned {} trigger conditions from rule {} to rule {}", 
                clonedConditions.size(), sourceRuleId, targetRuleId);
        return clonedConditions;
    }

    public boolean validateConditionJson(Map<String, Object> conditionJson) {
        if (conditionJson == null || conditionJson.isEmpty()) {
            return false;
        }

        String type = (String) conditionJson.get("type");
        if (type == null) {
            return false;
        }

        return switch (type) {
            case "SIMPLE" -> validateSimpleCondition(conditionJson);
            case "AND", "OR" -> validateCompositeCondition(conditionJson);
            default -> false;
        };
    }

    public String toHumanReadable(TriggerCondition condition) {
        try {
            Map<String, Object> json = objectMapper.readValue(
                    condition.getConditionJson(),
                    new TypeReference<Map<String, Object>>() {});
            return buildHumanReadable(json);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse condition JSON: {}", e.getMessage());
            return "Invalid condition";
        }
    }

    private TriggerCondition createCondition(Long ruleId, TriggerConditionRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request.getConditionJson());
            return TriggerCondition.builder()
                    .ruleId(ruleId)
                    .conditionJson(json)
                    .description(request.getDescription())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RulesException("Failed to serialize condition JSON", e);
        }
    }

    private boolean validateSimpleCondition(Map<String, Object> json) {
        String field = (String) json.get("field");
        String operator = (String) json.get("operator");
        Object value = json.get("value");

        if (field == null || field.isBlank()) {
            return false;
        }
        if (operator == null || !isValidOperator(operator)) {
            return false;
        }
        return value != null;
    }

    @SuppressWarnings("unchecked")
    private boolean validateCompositeCondition(Map<String, Object> json) {
        Object conditions = json.get("conditions");
        if (!(conditions instanceof List)) {
            return false;
        }

        List<Map<String, Object>> conditionList = (List<Map<String, Object>>) conditions;
        if (conditionList.isEmpty()) {
            return false;
        }

        for (Map<String, Object> condition : conditionList) {
            if (!validateConditionJson(condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidOperator(String operator) {
        return switch (operator) {
            case "EQUALS", "NOT_EQUALS", "GREATER_THAN", "GREATER_THAN_OR_EQUALS",
                 "LESS_THAN", "LESS_THAN_OR_EQUALS", "CONTAINS", "MATCHES", "IN", "NOT_IN" -> true;
            default -> false;
        };
    }

    @SuppressWarnings("unchecked")
    private String buildHumanReadable(Map<String, Object> json) {
        String type = (String) json.get("type");
        
        if ("SIMPLE".equals(type)) {
            String field = (String) json.get("field");
            String operator = (String) json.get("operator");
            Object value = json.get("value");
            return String.format("%s %s %s", field, operatorToSymbol(operator), formatValue(value));
        } else if ("AND".equals(type) || "OR".equals(type)) {
            List<Map<String, Object>> conditions = (List<Map<String, Object>>) json.get("conditions");
            if (conditions == null || conditions.isEmpty()) {
                return type + " (empty)";
            }
            List<String> parts = conditions.stream()
                    .map(this::buildHumanReadable)
                    .toList();
            String joiner = "AND".equals(type) ? " AND " : " OR ";
            return "(" + String.join(joiner, parts) + ")";
        }
        
        return json.toString();
    }

    private String operatorToSymbol(String operator) {
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

    private String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof List) {
            return value.toString();
        }
        return String.valueOf(value);
    }
}
