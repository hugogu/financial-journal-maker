package com.financial.coa.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal class representing a parsed row from an import file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRecord {
    
    private Integer rowNumber;
    private String code;
    private String name;
    private String parentCode;
    private String description;
    private Boolean sharedAcrossScenarios;
    
    /**
     * Check if this record has all required fields.
     */
    public boolean isValid() {
        return code != null && !code.isBlank() 
            && name != null && !name.isBlank();
    }
    
    /**
     * Check if this is a root account (no parent).
     */
    public boolean isRoot() {
        return parentCode == null || parentCode.isBlank();
    }
}
