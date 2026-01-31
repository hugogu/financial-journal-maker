package com.financial.rules.service;

import com.financial.rules.dto.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class NumscriptValidator {

    private static final Pattern VARS_BLOCK = Pattern.compile("vars\\s*\\{[^}]*\\}", Pattern.DOTALL);
    private static final Pattern SEND_BLOCK = Pattern.compile("send\\s*\\[[^]]*\\]\\s*\\([^)]*\\)", Pattern.DOTALL);
    private static final Pattern VARIABLE_DECL = Pattern.compile("(monetary|number|string|boolean)\\s+\\$([a-zA-Z_][a-zA-Z0-9_]*)");
    private static final Pattern VARIABLE_REF = Pattern.compile("\\$([a-zA-Z_][a-zA-Z0-9_]*)");
    private static final Pattern ACCOUNT_REF = Pattern.compile("@([a-zA-Z_][a-zA-Z0-9_:]*)?");
    
    private static final Set<String> VALID_TYPES = Set.of("monetary", "number", "string", "boolean");
    private static final Set<String> RESERVED_ACCOUNTS = Set.of("world");

    public ValidationResult validate(String numscript) {
        ValidationResult result = new ValidationResult();
        
        if (numscript == null || numscript.isBlank()) {
            result.addError("Numscript cannot be empty");
            return result;
        }

        validateStructure(numscript, result);
        
        if (result.isValid()) {
            validateVariables(numscript, result);
            validateAccounts(numscript, result);
            validateSyntax(numscript, result);
        }

        log.debug("Numscript validation result: valid={}, errors={}, warnings={}", 
                result.isValid(), result.getErrors().size(), result.getWarnings().size());
        
        return result;
    }

    public ValidationResult validateSyntax(String numscript) {
        return validate(numscript);
    }

    public ValidationResult validateAccountReferences(String numscript, Set<String> validAccounts) {
        ValidationResult result = new ValidationResult();
        
        Matcher matcher = ACCOUNT_REF.matcher(numscript);
        while (matcher.find()) {
            String account = matcher.group(1);
            if (account != null && !account.isEmpty()) {
                String accountCode = extractAccountCode(account);
                if (!RESERVED_ACCOUNTS.contains(accountCode) && 
                    validAccounts != null && !validAccounts.contains(accountCode)) {
                    result.addWarning("Account reference may not exist: @" + account);
                }
            }
        }
        
        return result;
    }

    private void validateStructure(String numscript, ValidationResult result) {
        boolean hasSend = SEND_BLOCK.matcher(numscript).find();

        if (!hasSend) {
            result.addError("Numscript must contain at least one 'send' statement");
        }

        int openBraces = countOccurrences(numscript, '{');
        int closeBraces = countOccurrences(numscript, '}');
        if (openBraces != closeBraces) {
            result.addError("Mismatched braces: " + openBraces + " '{' vs " + closeBraces + " '}'");
        }

        int openParens = countOccurrences(numscript, '(');
        int closeParens = countOccurrences(numscript, ')');
        if (openParens != closeParens) {
            result.addError("Mismatched parentheses: " + openParens + " '(' vs " + closeParens + " ')'");
        }

        int openBrackets = countOccurrences(numscript, '[');
        int closeBrackets = countOccurrences(numscript, ']');
        if (openBrackets != closeBrackets) {
            result.addError("Mismatched brackets: " + openBrackets + " '[' vs " + closeBrackets + " ']'");
        }
    }

    private void validateVariables(String numscript, ValidationResult result) {
        Set<String> declaredVars = new HashSet<>();
        Set<String> referencedVars = new HashSet<>();

        Matcher declMatcher = VARIABLE_DECL.matcher(numscript);
        while (declMatcher.find()) {
            String type = declMatcher.group(1);
            String name = declMatcher.group(2);
            
            if (!VALID_TYPES.contains(type)) {
                result.addError("Invalid variable type: " + type);
            }
            
            if (declaredVars.contains(name)) {
                result.addError("Duplicate variable declaration: $" + name);
            }
            declaredVars.add(name);
        }

        Matcher refMatcher = VARIABLE_REF.matcher(numscript);
        while (refMatcher.find()) {
            String name = refMatcher.group(1);
            referencedVars.add(name);
        }

        referencedVars.removeAll(declaredVars);
        for (String undeclared : referencedVars) {
            if (!isInVarsBlock(numscript, undeclared)) {
                result.addWarning("Variable $" + undeclared + " may not be declared");
            }
        }
    }

    private void validateAccounts(String numscript, ValidationResult result) {
        Matcher matcher = ACCOUNT_REF.matcher(numscript);
        while (matcher.find()) {
            String account = matcher.group(1);
            if (account == null || account.isEmpty()) {
                result.addError("Empty account reference found");
            }
        }
    }

    private void validateSyntax(String numscript, ValidationResult result) {
        String[] lines = numscript.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            if (line.contains("  ") && !line.startsWith(" ")) {
                result.addWarning("Line " + (i + 1) + ": Multiple consecutive spaces detected");
            }
        }
    }

    private boolean isInVarsBlock(String numscript, String varName) {
        Matcher matcher = VARS_BLOCK.matcher(numscript);
        if (matcher.find()) {
            String varsBlock = matcher.group();
            return varsBlock.contains("$" + varName);
        }
        return false;
    }

    private String extractAccountCode(String accountPath) {
        if (accountPath.contains(":")) {
            String[] parts = accountPath.split(":");
            return parts[parts.length - 1];
        }
        return accountPath;
    }

    private int countOccurrences(String str, char c) {
        int count = 0;
        for (char ch : str.toCharArray()) {
            if (ch == c) count++;
        }
        return count;
    }
}
