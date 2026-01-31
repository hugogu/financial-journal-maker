package com.financial.transactionflow.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * T043: DTO for real-time design preview state during AI sessions
 */
@Data
@Builder
public class DesignPreviewDto {
    
    private Long sessionId;
    private String currentPhase;
    
    /**
     * Accounts defined in current session (both confirmed and tentative)
     */
    private List<AccountPreviewDto> accounts;
    
    /**
     * Journal entries grouped by trigger event
     */
    private List<EntryGroupDto> entryGroups;
    
    /**
     * Current transaction type being designed
     */
    private String transactionTypeCode;
    private String transactionTypeName;
    
    /**
     * Summary statistics
     */
    private int confirmedAccountCount;
    private int tentativeAccountCount;
    private int confirmedEntryCount;
    private int tentativeEntryCount;
    
    /**
     * Whether the current design is valid (has required accounts and entries)
     */
    private Boolean isValid;
    
    /**
     * Validation messages if not valid
     */
    private List<String> validationMessages;
    
    @Data
    @Builder
    public static class AccountPreviewDto {
        private String accountCode;
        private String accountName;
        private String accountType;
        private boolean confirmed;
        private boolean linkedToCoA;
    }
    
    @Data
    @Builder
    public static class EntryGroupDto {
        private String triggerEvent;
        private List<EntryPreviewDto> entries;
    }
    
    @Data
    @Builder
    public static class EntryPreviewDto {
        private String operation;
        private String accountCode;
        private String accountName;
        private String amountExpression;
        private boolean confirmed;
    }
}
