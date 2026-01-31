package com.financial.transactionflow.dto;

/**
 * Enum representing the type of flow between accounts.
 */
public enum FlowType {
    CASH("Cash/Fund Flow", "solid"),
    INFO("Information Flow", "dashed");

    private final String displayName;
    private final String lineStyle;

    FlowType(String displayName, String lineStyle) {
        this.displayName = displayName;
        this.lineStyle = lineStyle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLineStyle() {
        return lineStyle;
    }
}
