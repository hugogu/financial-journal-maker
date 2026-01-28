package com.financial.coa.exception;

import java.util.List;

/**
 * Exception thrown when an import file fails validation.
 */
public class InvalidImportFileException extends CoaException {
    
    private final List<String> validationErrors;
    
    public InvalidImportFileException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    public InvalidImportFileException(String message) {
        super(message);
        this.validationErrors = List.of();
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
