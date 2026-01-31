package com.financial.domain.domain;

/**
 * Lifecycle status for domain entities (Product, Scenario, TransactionType).
 * Follows the same pattern as RuleStatus in the accounting-rules module.
 */
public enum EntityStatus {
    DRAFT,    // Initial state, fully editable
    ACTIVE,   // In use, limited editing
    ARCHIVED  // Soft deleted, read-only
}
