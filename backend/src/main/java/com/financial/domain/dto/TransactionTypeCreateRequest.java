package com.financial.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTypeCreateRequest {

    @NotNull(message = "Scenario ID is required")
    private Long scenarioId;

    @NotBlank(message = "Transaction type code is required")
    @Size(max = 50, message = "Transaction type code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Transaction type name is required")
    @Size(max = 200, message = "Transaction type name must not exceed 200 characters")
    private String name;

    private String description;
}
