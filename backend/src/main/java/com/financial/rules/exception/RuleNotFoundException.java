package com.financial.rules.exception;

public class RuleNotFoundException extends RulesException {
    
    public RuleNotFoundException(Long id) {
        super("Accounting rule not found with id: " + id, "RULE_NOT_FOUND");
    }
    
    public RuleNotFoundException(String code) {
        super("Accounting rule not found with code: " + code, "RULE_NOT_FOUND");
    }
}
