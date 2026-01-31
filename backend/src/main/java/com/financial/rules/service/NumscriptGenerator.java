package com.financial.rules.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.rules.domain.AccountingRule;
import com.financial.rules.domain.EntryLine;
import com.financial.rules.domain.EntryTemplate;
import com.financial.rules.domain.EntryType;
import com.financial.rules.dto.GenerationResponse;
import com.financial.rules.dto.ValidationResult;
import com.financial.rules.dto.VariableDefinition;
import com.financial.rules.exception.RulesException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NumscriptGenerator {

    private final NumscriptValidator validator;
    private final ObjectMapper objectMapper;

    public GenerationResponse generate(AccountingRule rule) {
        log.info("Generating Numscript for rule: {}", rule.getCode());

        ValidationResult preValidation = validateRuleForGeneration(rule);
        if (!preValidation.isValid()) {
            return GenerationResponse.withValidation(null, preValidation);
        }

        try {
            String numscript = buildNumscript(rule);
            ValidationResult syntaxValidation = validator.validate(numscript);
            
            return GenerationResponse.withValidation(numscript, syntaxValidation);
        } catch (Exception e) {
            log.error("Failed to generate Numscript for rule {}: {}", rule.getCode(), e.getMessage());
            return GenerationResponse.withValidation(null, 
                    ValidationResult.failure("Generation failed: " + e.getMessage()));
        }
    }

    private ValidationResult validateRuleForGeneration(AccountingRule rule) {
        ValidationResult result = new ValidationResult();

        if (rule.getEntryTemplate() == null) {
            result.addError("Rule has no entry template");
            return result;
        }

        if (rule.getEntryTemplate().getLines() == null || rule.getEntryTemplate().getLines().isEmpty()) {
            result.addError("Entry template has no lines");
            return result;
        }

        return result;
    }

    private String buildNumscript(AccountingRule rule) {
        StringBuilder sb = new StringBuilder();
        EntryTemplate template = rule.getEntryTemplate();

        List<VariableDefinition> variables = parseVariableSchema(template.getVariableSchemaJson());
        if (!variables.isEmpty()) {
            sb.append(buildVariablesSection(variables));
            sb.append("\n");
        }

        sb.append(buildSendSection(template.getLines()));

        return sb.toString();
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

    private String buildVariablesSection(List<VariableDefinition> variables) {
        StringBuilder sb = new StringBuilder();
        sb.append("vars {\n");
        
        for (VariableDefinition var : variables) {
            String numscriptType = mapTypeToNumscript(var);
            sb.append(String.format("  %s $%s\n", numscriptType, sanitizeVariableName(var.getName())));
        }
        
        sb.append("}\n");
        return sb.toString();
    }

    private String mapTypeToNumscript(VariableDefinition var) {
        return switch (var.getType()) {
            case MONEY -> {
                String currency = var.getCurrency() != null ? var.getCurrency() : "USD";
                yield "monetary";
            }
            case DECIMAL -> "number";
            case BOOLEAN -> "boolean";
            case STRING -> "string";
        };
    }

    private String buildSendSection(List<EntryLine> lines) {
        StringBuilder sb = new StringBuilder();

        List<EntryLine> debits = lines.stream()
                .filter(l -> l.getEntryType() == EntryType.DEBIT)
                .toList();
        List<EntryLine> credits = lines.stream()
                .filter(l -> l.getEntryType() == EntryType.CREDIT)
                .toList();

        if (debits.isEmpty() || credits.isEmpty()) {
            throw new RulesException("Entry template must have at least one debit and one credit line");
        }

        sb.append("send [\n");
        
        for (int i = 0; i < debits.size(); i++) {
            EntryLine debit = debits.get(i);
            String amount = translateExpression(debit.getAmountExpression());
            
            sb.append(String.format("  %s %s\n", "USD", amount));
        }
        
        sb.append("] (\n");
        sb.append("  source = {\n");
        
        for (EntryLine credit : credits) {
            String account = mapAccountToFormancePath(credit.getAccountCode());
            sb.append(String.format("    %s\n", account));
        }
        
        sb.append("  }\n");
        sb.append("  destination = {\n");
        
        for (EntryLine debit : debits) {
            String account = mapAccountToFormancePath(debit.getAccountCode());
            sb.append(String.format("    %s\n", account));
        }
        
        sb.append("  }\n");
        sb.append(")\n");

        return sb.toString();
    }

    private String translateExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return "0";
        }

        String result = expression
                .replaceAll("([a-z][a-z0-9_.]*)", "\\$$1")
                .replaceAll("\\$([0-9])", "$1");
        
        return result;
    }

    public String mapAccountToFormancePath(String accountCode) {
        if (accountCode == null || accountCode.isBlank()) {
            return "@unknown";
        }
        return "@account:" + accountCode.toLowerCase().replace("-", "_");
    }

    private String sanitizeVariableName(String name) {
        if (name == null) return "unknown";
        return name.replace(".", "_");
    }
}
