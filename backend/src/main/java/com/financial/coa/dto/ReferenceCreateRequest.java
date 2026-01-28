package com.financial.coa.dto;

import com.financial.coa.domain.AccountReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new account reference.
 * Used by rule/scenario modules to mark accounts as referenced.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceCreateRequest {
    
    @NotBlank(message = "Account code is required")
    @Size(max = 50, message = "Account code must not exceed 50 characters")
    private String accountCode;
    
    @NotBlank(message = "Reference source ID is required")
    @Size(max = 255, message = "Reference source ID must not exceed 255 characters")
    private String referenceSourceId;
    
    @NotNull(message = "Reference type is required")
    private AccountReference.ReferenceType referenceType;
    
    @Size(max = 500, message = "Reference description must not exceed 500 characters")
    private String referenceDescription;
}
