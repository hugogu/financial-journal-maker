package com.financial.coa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for account data.
 * Includes computed fields like hasChildren and isReferenced.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private String parentCode;
    private Boolean hasChildren;
    private Boolean isReferenced;
    private Integer referenceCount;
    private Boolean sharedAcrossScenarios;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
