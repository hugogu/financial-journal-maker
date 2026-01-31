package com.financial.rules.exception;

public class RulesException extends RuntimeException {
    
    private final String errorCode;
    
    public RulesException(String message) {
        super(message);
        this.errorCode = "RULES_ERROR";
    }
    
    public RulesException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public RulesException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RULES_ERROR";
    }
    
    public RulesException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
