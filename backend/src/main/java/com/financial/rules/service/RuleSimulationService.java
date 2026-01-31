package com.financial.rules.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.rules.domain.*;
import com.financial.rules.dto.*;
import com.financial.rules.repository.TriggerConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleSimulationService {

    private final TriggerConditionRepository conditionRepository;
    private final ConditionEvaluator conditionEvaluator;
    private final ExpressionParser expressionParser;
    private final ObjectMapper objectMapper;

    public SimulationResponse simulate(AccountingRule rule, Map<String, Object> eventData) {
        log.info("Simulating rule {} with event data", rule.getCode());

        List<TriggerCondition> conditions = conditionRepository.findByRuleId(rule.getId());
        
        if (!conditions.isEmpty()) {
            ConditionEvaluator.EvaluationResult evalResult = conditionEvaluator.evaluateAll(conditions, eventData);
            if (!evalResult.matches()) {
                String reason = evalResult.reason() != null 
                        ? evalResult.reason() 
                        : "Trigger conditions not met";
                return SimulationResponse.notFired(reason);
            }
        }

        try {
            List<SimulatedEntry> entries = evaluateEntries(rule.getEntryTemplate(), eventData);
            SimulationResponse response = SimulationResponse.fired(entries);

            if (!response.isBalanced()) {
                response.addWarning("Total debits do not equal total credits");
            }

            return response;
        } catch (Exception e) {
            log.error("Simulation failed for rule {}: {}", rule.getCode(), e.getMessage());
            SimulationResponse response = SimulationResponse.builder()
                    .wouldFire(true)
                    .totalDebits(BigDecimal.ZERO)
                    .totalCredits(BigDecimal.ZERO)
                    .isBalanced(false)
                    .build();
            response.addError("Simulation failed: " + e.getMessage());
            return response;
        }
    }

    private List<SimulatedEntry> evaluateEntries(EntryTemplate template, Map<String, Object> eventData) {
        List<SimulatedEntry> entries = new ArrayList<>();
        
        if (template == null || template.getLines() == null) {
            return entries;
        }

        List<VariableDefinition> schema = parseVariableSchema(template.getVariableSchemaJson());

        for (EntryLine line : template.getLines()) {
            BigDecimal amount = evaluateExpression(line.getAmountExpression(), eventData);
            String memo = resolveMemoTemplate(line.getMemoTemplate(), eventData);

            SimulatedEntry entry = SimulatedEntry.builder()
                    .accountCode(line.getAccountCode())
                    .accountName(resolveAccountName(line.getAccountCode()))
                    .entryType(line.getEntryType())
                    .amount(amount)
                    .currency("USD")
                    .memo(memo)
                    .build();

            entries.add(entry);
        }

        return entries;
    }

    private List<VariableDefinition> parseVariableSchema(String json) {
        if (json == null || json.isBlank() || "[]".equals(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<VariableDefinition>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse variable schema: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private BigDecimal evaluateExpression(String expression, Map<String, Object> eventData) {
        if (expression == null || expression.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return expressionParser.evaluate(expression, eventData);
        } catch (Exception e) {
            log.warn("Failed to evaluate expression '{}': {}", expression, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private String resolveMemoTemplate(String template, Map<String, Object> eventData) {
        if (template == null || template.isBlank()) {
            return null;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : eventData.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
            
            String simplePlaceholder = "{" + entry.getKey() + "}";
            if (result.contains(simplePlaceholder)) {
                result = result.replace(simplePlaceholder, String.valueOf(entry.getValue()));
            }
        }

        return result;
    }

    private String resolveAccountName(String accountCode) {
        return "Account " + accountCode;
    }
}
