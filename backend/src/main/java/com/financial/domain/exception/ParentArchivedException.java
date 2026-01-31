package com.financial.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a child entity under an archived parent.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ParentArchivedException extends DomainException {
    
    public ParentArchivedException(String parentType, String childType) {
        super("Cannot create " + childType + " under an archived " + parentType);
    }
}
