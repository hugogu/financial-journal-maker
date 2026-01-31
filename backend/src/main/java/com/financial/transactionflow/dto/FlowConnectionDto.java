package com.financial.transactionflow.dto;

/**
 * Edge representing fund or information flow between accounts.
 */
public class FlowConnectionDto {
    private String connectionId;
    private String sourceAccountCode;
    private String targetAccountCode;
    private FlowType flowType;
    private String amountExpression;
    private String label;
    private String triggerEvent;
    private String condition;

    public FlowConnectionDto() {
    }

    // Getters and Setters
    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getSourceAccountCode() {
        return sourceAccountCode;
    }

    public void setSourceAccountCode(String sourceAccountCode) {
        this.sourceAccountCode = sourceAccountCode;
    }

    public String getTargetAccountCode() {
        return targetAccountCode;
    }

    public void setTargetAccountCode(String targetAccountCode) {
        this.targetAccountCode = targetAccountCode;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public String getAmountExpression() {
        return amountExpression;
    }

    public void setAmountExpression(String amountExpression) {
        this.amountExpression = amountExpression;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
