package com.financial.ai.exception;

public class MaxSessionsExceededException extends RuntimeException {

    private static final int MAX_SESSIONS = 5;

    public MaxSessionsExceededException(String analystId) {
        super("Analyst " + analystId + " has reached maximum concurrent sessions limit of " + MAX_SESSIONS);
    }

    public MaxSessionsExceededException(String analystId, int maxSessions) {
        super("Analyst " + analystId + " has reached maximum concurrent sessions limit of " + maxSessions);
    }
}
