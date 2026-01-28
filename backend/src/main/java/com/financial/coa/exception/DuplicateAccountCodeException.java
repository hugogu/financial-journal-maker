package com.financial.coa.exception;

/**
 * Exception thrown when attempting to create an account with a code that already exists.
 */
public class DuplicateAccountCodeException extends CoaException {
    
    private final String accountCode;
    
    public DuplicateAccountCodeException(String accountCode) {
        super(String.format("Account with code '%s' already exists", accountCode));
        this.accountCode = accountCode;
    }
    
    public String getAccountCode() {
        return accountCode;
    }
}
