package com.financial.coa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing account mapping.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingUpdateRequest {
    
    @NotBlank(message = "Formance Ledger account is required")
    @Size(max = 255, message = "Formance Ledger account must not exceed 255 characters")
    private String formanceLedgerAccount;
    
    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}
