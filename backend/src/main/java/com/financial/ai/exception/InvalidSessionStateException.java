package com.financial.ai.exception;

import com.financial.ai.domain.SessionStatus;

public class InvalidSessionStateException extends RuntimeException {

    public InvalidSessionStateException(SessionStatus currentStatus, String action) {
        super("Cannot " + action + " session in " + currentStatus + " status");
    }

    public InvalidSessionStateException(String message) {
        super(message);
    }
}
