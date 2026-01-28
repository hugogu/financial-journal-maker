package com.financial.coa.exception;

/**
 * Exception thrown when a circular reference is detected in the account hierarchy.
 */
public class CircularReferenceException extends CoaException {
    
    private final String accountCode;
    private final String parentCode;
    
    public CircularReferenceException(String accountCode, String parentCode) {
        super(String.format("Circular reference detected: account '%s' cannot have parent '%s'", 
                accountCode, parentCode));
        this.accountCode = accountCode;
        this.parentCode = parentCode;
    }
    
    public String getAccountCode() {
        return accountCode;
    }
    
    public String getParentCode() {
        return parentCode;
    }
}
