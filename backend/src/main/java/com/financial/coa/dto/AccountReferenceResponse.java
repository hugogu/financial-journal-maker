package com.financial.coa.dto;

import com.financial.coa.domain.AccountReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for account reference data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReferenceResponse {
    
    private Long id;
    private String accountCode;
    private String referenceSourceId;
    private AccountReference.ReferenceType referenceType;
    private String referenceDescription;
    private LocalDateTime createdAt;
}
