package com.financial.transactionflow.dto;

import lombok.Builder;
import lombok.Data;

/**
 * T036: DTO for Numscript DSL view with validation status
 */
@Data
@Builder
public class NumscriptViewDto {
    
    private String transactionTypeCode;
    private String transactionTypeName;
    
    /**
     * The complete Numscript code for this transaction
     */
    private String numscriptCode;
    
    /**
     * Whether the Numscript passes validation
     */
    private Boolean numscriptValid;
    
    /**
     * Validation error message if numscriptValid is false
     */
    private String validationError;
    
    /**
     * Source of the Numscript (EXPORT_ARTIFACT or GENERATED)
     */
    private String source;
    
    /**
     * When the Numscript was last updated
     */
    private String lastUpdated;
    
    /**
     * Lines of code count for display
     */
    private Integer lineCount;
}
