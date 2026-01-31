package com.financial.domain.exception;

import com.financial.domain.domain.EntityStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invalid state transition is attempted.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStateTransitionException extends DomainException {
    
    public InvalidStateTransitionException(String entityType, EntityStatus currentStatus, String action) {
        super("Cannot " + action + " " + entityType + " in " + currentStatus + " status");
    }
}
