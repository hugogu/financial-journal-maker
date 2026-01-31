package com.financial.rules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpressionValidationResponse {

    private boolean valid;
    private ExpressionType parsedType;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    public static ExpressionValidationResponse fromParserResult(
            com.financial.rules.service.ExpressionParser.ValidationResult result) {
        return ExpressionValidationResponse.builder()
                .valid(result.valid())
                .parsedType(result.parsedType())
                .errors(result.errors() != null ? result.errors() : new ArrayList<>())
                .warnings(result.warnings() != null ? result.warnings() : new ArrayList<>())
                .build();
    }
}
