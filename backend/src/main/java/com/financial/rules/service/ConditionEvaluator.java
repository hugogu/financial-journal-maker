package com.financial.rules.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.rules.domain.TriggerCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionEvaluator {

    private final ObjectMapper objectMapper;

    public EvaluationResult evaluate(TriggerCondition condition, Map<String, Object> eventData) {
        try {
            Map<String, Object> json = objectMapper.readValue(
                    condition.getConditionJson(),
                    new TypeReference<Map<String, Object>>() {});
            boolean result = evaluateCondition(json, eventData);
            return new EvaluationResult(result, null);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse condition JSON: {}", e.getMessage());
            return new EvaluationResult(false, "Invalid condition JSON: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error evaluating condition: {}", e.getMessage());
            return new EvaluationResult(false, "Evaluation error: " + e.getMessage());
        }
    }

    public EvaluationResult evaluateAll(List<TriggerCondition> conditions, Map<String, Object> eventData) {
        if (conditions == null || conditions.isEmpty()) {
            return new EvaluationResult(true, null);
        }

        for (TriggerCondition condition : conditions) {
            EvaluationResult result = evaluate(condition, eventData);
            if (!result.matches()) {
                return result;
            }
        }
        return new EvaluationResult(true, null);
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Map<String, Object> json, Map<String, Object> eventData) {
        String type = (String) json.get("type");
        
        return switch (type) {
            case "SIMPLE" -> evaluateSimple(json, eventData);
            case "AND" -> {
                List<Map<String, Object>> conditions = (List<Map<String, Object>>) json.get("conditions");
                yield conditions.stream().allMatch(c -> evaluateCondition(c, eventData));
            }
            case "OR" -> {
                List<Map<String, Object>> conditions = (List<Map<String, Object>>) json.get("conditions");
                yield conditions.stream().anyMatch(c -> evaluateCondition(c, eventData));
            }
            default -> {
                log.warn("Unknown condition type: {}", type);
                yield false;
            }
        };
    }

    private boolean evaluateSimple(Map<String, Object> json, Map<String, Object> eventData) {
        String field = (String) json.get("field");
        String operator = (String) json.get("operator");
        Object expectedValue = json.get("value");

        Object actualValue = resolveField(field, eventData);
        if (actualValue == null && !"NOT_EQUALS".equals(operator) && !"NOT_IN".equals(operator)) {
            return false;
        }

        return switch (operator) {
            case "EQUALS" -> equals(actualValue, expectedValue);
            case "NOT_EQUALS" -> !equals(actualValue, expectedValue);
            case "GREATER_THAN" -> compare(actualValue, expectedValue) > 0;
            case "GREATER_THAN_OR_EQUALS" -> compare(actualValue, expectedValue) >= 0;
            case "LESS_THAN" -> compare(actualValue, expectedValue) < 0;
            case "LESS_THAN_OR_EQUALS" -> compare(actualValue, expectedValue) <= 0;
            case "CONTAINS" -> contains(actualValue, expectedValue);
            case "MATCHES" -> matches(actualValue, expectedValue);
            case "IN" -> isIn(actualValue, expectedValue);
            case "NOT_IN" -> !isIn(actualValue, expectedValue);
            default -> {
                log.warn("Unknown operator: {}", operator);
                yield false;
            }
        };
    }

    private Object resolveField(String field, Map<String, Object> data) {
        if (field == null || data == null) {
            return null;
        }

        String[] parts = field.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private boolean equals(Object actual, Object expected) {
        if (actual == null && expected == null) return true;
        if (actual == null || expected == null) return false;
        
        if (actual instanceof Number && expected instanceof Number) {
            return new BigDecimal(actual.toString()).compareTo(new BigDecimal(expected.toString())) == 0;
        }
        
        return actual.toString().equals(expected.toString());
    }

    private int compare(Object actual, Object expected) {
        if (actual == null || expected == null) return -1;
        
        try {
            BigDecimal actualNum = new BigDecimal(actual.toString());
            BigDecimal expectedNum = new BigDecimal(expected.toString());
            return actualNum.compareTo(expectedNum);
        } catch (NumberFormatException e) {
            return actual.toString().compareTo(expected.toString());
        }
    }

    private boolean contains(Object actual, Object expected) {
        if (actual == null || expected == null) return false;
        return actual.toString().contains(expected.toString());
    }

    private boolean matches(Object actual, Object expected) {
        if (actual == null || expected == null) return false;
        try {
            return Pattern.matches(expected.toString(), actual.toString());
        } catch (Exception e) {
            log.warn("Invalid regex pattern: {}", expected);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isIn(Object actual, Object expected) {
        if (actual == null || expected == null) return false;
        
        if (expected instanceof List) {
            List<Object> list = (List<Object>) expected;
            return list.stream().anyMatch(item -> equals(actual, item));
        }
        
        return equals(actual, expected);
    }

    public record EvaluationResult(boolean matches, String reason) {}
}
