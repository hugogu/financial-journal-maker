package com.financial.transactionflow.dto;

import java.time.Instant;

/**
 * Aggregated view of a scenario under a product.
 */
public class ScenarioSummary {
    private String scenarioCode;
    private String scenarioName;
    private String description;
    private String productCode;
    private Integer transactionTypeCount;
    private Long sourceSessionId;
    private Instant createdAt;

    public ScenarioSummary() {
    }

    // Getters and Setters
    public String getScenarioCode() {
        return scenarioCode;
    }

    public void setScenarioCode(String scenarioCode) {
        this.scenarioCode = scenarioCode;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
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

    public Integer getTransactionTypeCount() {
        return transactionTypeCount;
    }

    public void setTransactionTypeCount(Integer transactionTypeCount) {
        this.transactionTypeCount = transactionTypeCount;
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
}
