package com.financial.coa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {
    
    @NotBlank(message = "Account code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9.-]+$", message = "Code can only contain alphanumeric characters, dots, and hyphens")
    private String code;
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 50, message = "Parent code must not exceed 50 characters")
    private String parentCode;
    
    @Builder.Default
    private Boolean sharedAcrossScenarios = false;
}
