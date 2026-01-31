package com.financial.rules.dto;

import com.financial.rules.domain.EntryType;
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
public class EntryLineRequest {

    @NotBlank(message = "Account code is required")
    @Size(max = 50, message = "Account code must not exceed 50 characters")
    private String accountCode;

    @NotNull(message = "Entry type is required")
    private EntryType entryType;

    @NotBlank(message = "Amount expression is required")
    @Size(max = 500, message = "Amount expression must not exceed 500 characters")
    private String amountExpression;

    @Size(max = 255, message = "Memo template must not exceed 255 characters")
    private String memoTemplate;
}
