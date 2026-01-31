package com.financial.transactionflow.dto;

/**
 * Enum representing account types for visualization and categorization.
 * Used for color-coding in flow diagrams.
 */
public enum AccountType {
    CUSTOMER("Customer Account", "#14B8A6"),
    BANK("Bank/Settlement Account", "#9CA3AF"),
    CHANNEL("Payment Channel Account", "#3B82F6"),
    REVENUE("Revenue/P&L Account", "#22C55E"),
    COST("Cost/Expense Account", "#F472B6"),
    OTHER("Other Account", "#6B7280");

    private final String displayName;
    private final String color;

    AccountType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}
