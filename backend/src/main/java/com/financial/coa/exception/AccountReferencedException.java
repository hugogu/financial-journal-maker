package com.financial.coa.exception;

/**
 * Exception thrown when attempting to modify or delete an account that is referenced.
 */
public class AccountReferencedException extends CoaException {
    
    private final String accountCode;
    private final int referenceCount;
    
    public AccountReferencedException(String accountCode, int referenceCount, String operation) {
        super(String.format("Cannot %s account '%s': referenced by %d rule(s)/scenario(s)", 
                operation, accountCode, referenceCount));
        this.accountCode = accountCode;
        this.referenceCount = referenceCount;
    }
    
    public String getAccountCode() {
        return accountCode;
    }
    
    public int getReferenceCount() {
        return referenceCount;
    }
}
