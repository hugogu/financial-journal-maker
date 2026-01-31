package com.financial.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested entity is not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends DomainException {
    
    public EntityNotFoundException(String entityType, Long id) {
        super(entityType + " not found with id: " + id);
    }
    
    public EntityNotFoundException(String entityType, String code) {
        super(entityType + " not found with code: " + code);
    }
}
