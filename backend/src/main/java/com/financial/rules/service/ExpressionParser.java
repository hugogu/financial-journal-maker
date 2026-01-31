package com.financial.rules.service;

import com.financial.rules.dto.ExpressionType;
import com.financial.rules.dto.VariableDefinition;
import com.financial.rules.exception.ExpressionParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ExpressionParser {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-z][a-z0-9_.]*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Set<String> OPERATORS = Set.of("+", "-", "*", "/");
    private static final Set<Character> VALID_CHARS = Set.of('+', '-', '*', '/', '(', ')', '.', ' ');

    public ValidationResult validate(String expression, List<VariableDefinition> schema) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        ExpressionType resultType = null;

        if (expression == null || expression.isBlank()) {
            errors.add("Expression cannot be empty");
            return new ValidationResult(false, null, errors, warnings);
        }

        try {
            Set<String> variables = extractVariables(expression);
            Map<String, VariableDefinition> schemaMap = buildSchemaMap(schema);

            for (String variable : variables) {
                if (!schemaMap.containsKey(variable)) {
                    warnings.add("Variable '" + variable + "' is not defined in the schema");
                }
            }

            resultType = inferType(expression, schemaMap);
            validateSyntax(expression);

        } catch (ExpressionParseException e) {
            errors.add(e.getMessage());
        } catch (Exception e) {
            errors.add("Expression validation failed: " + e.getMessage());
        }

        return new ValidationResult(errors.isEmpty(), resultType, errors, warnings);
    }

    public Set<String> extractVariables(String expression) {
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        
        while (matcher.find()) {
            String match = matcher.group();
            if (!isKeyword(match)) {
                variables.add(match);
            }
        }
        
        return variables;
    }

    public ExpressionType getType(String expression, List<VariableDefinition> schema) {
        Map<String, VariableDefinition> schemaMap = buildSchemaMap(schema);
        return inferType(expression, schemaMap);
    }

    public BigDecimal evaluate(String expression, Map<String, Object> context) {
        String resolved = resolveVariables(expression, context);
        return evaluateArithmetic(resolved);
    }

    private void validateSyntax(String expression) {
        int parenCount = 0;
        boolean lastWasOperator = true;
        
        String trimmed = expression.trim();
        int i = 0;
        
        while (i < trimmed.length()) {
            char c = trimmed.charAt(i);
            
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            
            if (c == '(') {
                parenCount++;
                lastWasOperator = true;
                i++;
            } else if (c == ')') {
                parenCount--;
                if (parenCount < 0) {
                    throw new ExpressionParseException("Unmatched closing parenthesis", expression, i);
                }
                lastWasOperator = false;
                i++;
            } else if (OPERATORS.contains(String.valueOf(c))) {
                if (lastWasOperator && c != '-') {
                    throw new ExpressionParseException("Unexpected operator", expression, i, "operand");
                }
                lastWasOperator = true;
                i++;
            } else if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < trimmed.length() && (Character.isDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '.')) {
                    num.append(trimmed.charAt(i));
                    i++;
                }
                lastWasOperator = false;
            } else if (Character.isLetter(c) || c == '_') {
                StringBuilder var = new StringBuilder();
                while (i < trimmed.length() && (Character.isLetterOrDigit(trimmed.charAt(i)) || trimmed.charAt(i) == '_' || trimmed.charAt(i) == '.')) {
                    var.append(trimmed.charAt(i));
                    i++;
                }
                lastWasOperator = false;
            } else {
                throw new ExpressionParseException("Invalid character: " + c, expression, i);
            }
        }
        
        if (parenCount != 0) {
            throw new ExpressionParseException("Unmatched opening parenthesis", expression, expression.length() - 1);
        }
        
        if (lastWasOperator) {
            throw new ExpressionParseException("Expression ends with operator", expression, expression.length() - 1, "operand");
        }
    }

    private ExpressionType inferType(String expression, Map<String, VariableDefinition> schema) {
        Set<String> variables = extractVariables(expression);
        
        boolean hasMoney = false;
        boolean hasDecimal = false;
        
        for (String var : variables) {
            VariableDefinition def = schema.get(var);
            if (def != null) {
                if (def.getType() == ExpressionType.MONEY) {
                    hasMoney = true;
                } else if (def.getType() == ExpressionType.DECIMAL) {
                    hasDecimal = true;
                }
            }
        }
        
        if (hasMoney) {
            return ExpressionType.MONEY;
        } else if (hasDecimal || !variables.isEmpty()) {
            return ExpressionType.DECIMAL;
        }
        
        return ExpressionType.DECIMAL;
    }

    private Map<String, VariableDefinition> buildSchemaMap(List<VariableDefinition> schema) {
        Map<String, VariableDefinition> map = new HashMap<>();
        if (schema != null) {
            for (VariableDefinition def : schema) {
                map.put(def.getName(), def);
            }
        }
        return map;
    }

    private boolean isKeyword(String word) {
        return Set.of("true", "false", "null").contains(word.toLowerCase());
    }

    private String resolveVariables(String expression, Map<String, Object> context) {
        String result = expression;
        
        List<String> sortedVars = new ArrayList<>(context.keySet());
        sortedVars.sort((a, b) -> b.length() - a.length());
        
        for (String var : sortedVars) {
            Object value = context.get(var);
            if (value instanceof Number) {
                result = result.replace(var, value.toString());
            }
        }
        
        return result;
    }

    private BigDecimal evaluateArithmetic(String expression) {
        try {
            return evaluateExpression(expression.replaceAll("\\s+", ""), 0).value;
        } catch (Exception e) {
            throw new ExpressionParseException("Failed to evaluate expression: " + e.getMessage(), expression);
        }
    }

    private EvalResult evaluateExpression(String expr, int start) {
        BigDecimal result = BigDecimal.ZERO;
        char operator = '+';
        int i = start;
        
        while (i < expr.length()) {
            char c = expr.charAt(i);
            
            if (c == '(') {
                EvalResult inner = evaluateExpression(expr, i + 1);
                result = applyOperator(result, inner.value, operator);
                i = inner.nextIndex;
            } else if (c == ')') {
                return new EvalResult(result, i + 1);
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                operator = c;
                i++;
            } else if (Character.isDigit(c) || (c == '-' && (i == start || "+-*/(".indexOf(expr.charAt(i-1)) >= 0))) {
                StringBuilder num = new StringBuilder();
                if (c == '-') {
                    num.append(c);
                    i++;
                }
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i));
                    i++;
                }
                BigDecimal value = new BigDecimal(num.toString());
                result = applyOperator(result, value, operator);
            } else {
                i++;
            }
        }
        
        return new EvalResult(result, i);
    }

    private BigDecimal applyOperator(BigDecimal left, BigDecimal right, char op) {
        return switch (op) {
            case '+' -> left.add(right);
            case '-' -> left.subtract(right);
            case '*' -> left.multiply(right);
            case '/' -> left.divide(right, 10, java.math.RoundingMode.HALF_UP);
            default -> right;
        };
    }

    private record EvalResult(BigDecimal value, int nextIndex) {}

    public record ValidationResult(
            boolean valid,
            ExpressionType parsedType,
            List<String> errors,
            List<String> warnings
    ) {}
}
