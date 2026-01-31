package com.financial.transactionflow.dto;

import java.time.Instant;
import java.util.List;

/**
 * Complete view model for a transaction type with all accounting details.
 */
public class TransactionFlowView {
    private String transactionTypeCode;
    private String transactionTypeName;
    private String description;
    private String productCode;
    private String scenarioCode;
    private List<AccountNodeDto> accounts;
    private List<JournalEntryDisplayDto> journalEntries;
    private List<FlowConnectionDto> flowConnections;
    private String numscript;
    private boolean numscriptValid;
    private Long sourceSessionId;
    private Instant createdAt;
    private Instant updatedAt;

    public TransactionFlowView() {
    }

    // Getters and Setters
    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }

    public String getTransactionTypeName() {
        return transactionTypeName;
    }

    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getScenarioCode() {
        return scenarioCode;
    }

    public void setScenarioCode(String scenarioCode) {
        this.scenarioCode = scenarioCode;
    }

    public List<AccountNodeDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountNodeDto> accounts) {
        this.accounts = accounts;
    }

    public List<JournalEntryDisplayDto> getJournalEntries() {
        return journalEntries;
    }

    public void setJournalEntries(List<JournalEntryDisplayDto> journalEntries) {
        this.journalEntries = journalEntries;
    }

    public List<FlowConnectionDto> getFlowConnections() {
        return flowConnections;
    }

    public void setFlowConnections(List<FlowConnectionDto> flowConnections) {
        this.flowConnections = flowConnections;
    }

    public String getNumscript() {
        return numscript;
    }

    public void setNumscript(String numscript) {
        this.numscript = numscript;
    }

    public boolean isNumscriptValid() {
        return numscriptValid;
    }

    public void setNumscriptValid(boolean numscriptValid) {
        this.numscriptValid = numscriptValid;
    }

    public Long getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(Long sourceSessionId) {
        this.sourceSessionId = sourceSessionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
