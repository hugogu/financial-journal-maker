package com.financial.coa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for account mapping data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingResponse {
    
    private Long id;
    private String accountCode;
    private String formanceLedgerAccount;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
