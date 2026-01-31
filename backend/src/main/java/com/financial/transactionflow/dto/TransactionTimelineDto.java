package com.financial.transactionflow.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * T066: DTO for transaction timeline visualization
 */
@Data
@Builder
public class TransactionTimelineDto {
    
    private String transactionTypeCode;
    private String transactionTypeName;
    
    /**
     * Events in chronological order
     */
    private List<TimelineEventDto> events;
    
    /**
     * Whether this transaction has multi-day settlement
     */
    private Boolean hasMultipleTimings;
    
    @Data
    @Builder
    public static class TimelineEventDto {
        /**
         * Timing offset (T+0, T+1, T+2, etc.)
         */
        private String timing;
        
        /**
         * Description of what happens at this timing
         */
        private String description;
        
        /**
         * Related journal entries
         */
        private List<String> relatedEntryIds;
        
        /**
         * Whether this is a settlement event
         */
        private Boolean isSettlement;
    }
}
