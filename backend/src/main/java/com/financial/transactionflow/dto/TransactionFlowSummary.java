package com.financial.transactionflow.dto;

/**
 * Summary view of a transaction flow for list displays.
 */
public class TransactionFlowSummary {
    private String transactionTypeCode;
    private String transactionTypeName;
    private String description;
    private String productCode;
    private String scenarioCode;
    private Integer accountCount;
    private Integer entryCount;
    private boolean hasNumscript;
    private Long sourceSessionId;

    public TransactionFlowSummary() {
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

    public Integer getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(Integer accountCount) {
        this.accountCount = accountCount;
    }

    public Integer getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(Integer entryCount) {
        this.entryCount = entryCount;
    }

    public boolean isHasNumscript() {
        return hasNumscript;
    }

    public void setHasNumscript(boolean hasNumscript) {
        this.hasNumscript = hasNumscript;
    }

    public Long getSourceSessionId() {
        return sourceSessionId;
    }

    public void setSourceSessionId(Long sourceSessionId) {
        this.sourceSessionId = sourceSessionId;
    }
}
