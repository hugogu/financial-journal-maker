package com.financial.coa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for import operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequestDto {
    
    private String fileName;
    
    @Builder.Default
    private Boolean validateOnly = false;
}
