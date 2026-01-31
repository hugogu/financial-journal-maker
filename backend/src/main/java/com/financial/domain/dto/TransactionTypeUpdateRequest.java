package com.financial.domain.dto;

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
public class TransactionTypeUpdateRequest {

    @Size(max = 200, message = "Transaction type name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}
