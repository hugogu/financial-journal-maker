package com.financial.transactionflow.dto;

import java.time.Instant;

/**
 * Aggregated view of a product with counts.
 */
public class ProductSummary {
    private String productCode;
    private String productName;
    private String description;
    private Integer scenarioCount;
    private Integer transactionTypeCount;
    private Long sourceSessionId;
    private Instant createdAt;

    public ProductSummary() {
    }

    // Getters and Setters
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getScenarioCount() {
        return scenarioCount;
    }

    public void setScenarioCount(Integer scenarioCount) {
        this.scenarioCount = scenarioCount;
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
