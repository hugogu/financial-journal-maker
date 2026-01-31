package com.financial.transactionflow.dto;

/**
 * Presentation model for a single journal entry line (debit or credit).
 */
public class JournalEntryDisplayDto {
    private String entryId;
    private String operation; // DR or CR
    private String accountCode;
    private String accountName;
    private String amountExpression;
    private String triggerEvent;
    private String triggerEventLabel;
    private String condition;
    private int sequenceNumber;
    private String groupId;

    public JournalEntryDisplayDto() {
    }

    // Getters and Setters
    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAmountExpression() {
        return amountExpression;
    }

    public void setAmountExpression(String amountExpression) {
        this.amountExpression = amountExpression;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    public String getTriggerEventLabel() {
        return triggerEventLabel;
    }

    public void setTriggerEventLabel(String triggerEventLabel) {
        this.triggerEventLabel = triggerEventLabel;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
