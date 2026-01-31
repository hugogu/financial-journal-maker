package com.financial.rules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerationResponse {

    private String numscript;
    private ValidationResult validationResult;

    public static GenerationResponse success(String numscript) {
        return GenerationResponse.builder()
                .numscript(numscript)
                .validationResult(ValidationResult.success())
                .build();
    }

    public static GenerationResponse withValidation(String numscript, ValidationResult validation) {
        return GenerationResponse.builder()
                .numscript(numscript)
                .validationResult(validation)
                .build();
    }
}
