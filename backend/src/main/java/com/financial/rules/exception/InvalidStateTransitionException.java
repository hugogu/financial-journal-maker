package com.financial.rules.exception;

public class InvalidStateTransitionException extends RulesException {
    
    private final String currentStatus;
    private final String targetStatus;
    
    public InvalidStateTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Invalid state transition from %s to %s", currentStatus, targetStatus), 
              "INVALID_STATE_TRANSITION");
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public InvalidStateTransitionException(String currentStatus, String targetStatus, String reason) {
        super(String.format("Invalid state transition from %s to %s: %s", currentStatus, targetStatus, reason),
              "INVALID_STATE_TRANSITION");
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public String getTargetStatus() {
        return targetStatus;
    }
}
