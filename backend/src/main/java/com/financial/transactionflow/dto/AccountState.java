package com.financial.transactionflow.dto;

/**
 * Enum representing account states for visualization.
 * Used for border styling in flow diagrams.
 */
public enum AccountState {
    AVAILABLE("Available", "solid"),
    FROZEN("Frozen", "dotted"),
    IN_TRANSIT("In Transit", "dashed");

    private final String displayName;
    private final String borderStyle;

    AccountState(String displayName, String borderStyle) {
        this.displayName = displayName;
        this.borderStyle = borderStyle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBorderStyle() {
        return borderStyle;
    }
}
