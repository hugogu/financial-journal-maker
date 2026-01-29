package com.financial.coa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new account mapping.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingCreateRequest {
    
    @NotBlank(message = "Account code is required")
    @Size(max = 50, message = "Account code must not exceed 50 characters")
    private String accountCode;
    
    @NotBlank(message = "Formance Ledger account is required")
    @Size(max = 255, message = "Formance Ledger account must not exceed 255 characters")
    private String formanceLedgerAccount;
}
