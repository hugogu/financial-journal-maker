package com.financial.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to delete an entity that has child entities.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class HasChildrenException extends DomainException {
    
    public HasChildrenException(String entityType, String childType, int count) {
        super("Cannot delete " + entityType + " - it has " + count + " " + childType + "(s)");
    }
}
