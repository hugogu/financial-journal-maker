package com.financial.coa.exception;

/**
 * Base exception class for all Chart of Accounts Management exceptions.
 */
public class CoaException extends RuntimeException {
    
    public CoaException(String message) {
        super(message);
    }
    
    public CoaException(String message, Throwable cause) {
        super(message, cause);
    }
}
