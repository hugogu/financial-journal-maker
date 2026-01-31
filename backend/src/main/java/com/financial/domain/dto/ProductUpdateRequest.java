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
public class ProductUpdateRequest {

    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    private String description;

    private String businessModel;

    private String participants;

    private String fundFlow;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}
