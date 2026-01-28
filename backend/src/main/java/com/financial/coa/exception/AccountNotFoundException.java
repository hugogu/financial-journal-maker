package com.financial.coa.exception;

/**
 * Exception thrown when an account with the specified code is not found.
 */
public class AccountNotFoundException extends CoaException {
    
    private final String accountCode;
    
    public AccountNotFoundException(String accountCode) {
        super(String.format("Account not found: %s", accountCode));
        this.accountCode = accountCode;
    }
    
    public String getAccountCode() {
        return accountCode;
    }
}
