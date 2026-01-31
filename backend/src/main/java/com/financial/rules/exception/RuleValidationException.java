package com.financial.rules.exception;

import java.util.ArrayList;
import java.util.List;

public class RuleValidationException extends RulesException {
    
    private final List<ValidationError> validationErrors;
    
    public RuleValidationException(String message) {
        super(message, "VALIDATION_FAILED");
        this.validationErrors = new ArrayList<>();
    }
    
    public RuleValidationException(String message, List<ValidationError> errors) {
        super(message, "VALIDATION_FAILED");
        this.validationErrors = errors != null ? errors : new ArrayList<>();
    }
    
    public RuleValidationException(String field, String message) {
        super(message, "VALIDATION_FAILED");
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(new ValidationError(field, message, null));
    }
    
    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
    
    public void addError(String field, String message, String value) {
        this.validationErrors.add(new ValidationError(field, message, value));
    }
    
    public record ValidationError(String field, String message, String value) {}
}
