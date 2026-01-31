package com.financial.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create an entity with a duplicate code.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCodeException extends DomainException {
    
    public DuplicateCodeException(String entityType, String code) {
        super(entityType + " with code '" + code + "' already exists");
    }
}
