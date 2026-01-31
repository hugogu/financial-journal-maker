package com.financial.ai.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(Long sessionId) {
        super("Session not found with id: " + sessionId);
    }

    public SessionNotFoundException(String message) {
        super(message);
    }
}
