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
public class ValidationResult {

    @Builder.Default
    private boolean valid = true;
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }

    public static ValidationResult failure(String error) {
        return ValidationResult.builder()
                .valid(false)
                .errors(List.of(error))
                .build();
    }

    public static ValidationResult failure(List<String> errors) {
        return ValidationResult.builder()
                .valid(false)
                .errors(errors)
                .build();
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.valid = false;
    }

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}
