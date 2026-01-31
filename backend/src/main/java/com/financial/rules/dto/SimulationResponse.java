package com.financial.rules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResponse {

    private boolean wouldFire;
    private String reasonNotFired;
    
    @Builder.Default
    private List<SimulatedEntry> entries = new ArrayList<>();
    
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private boolean isBalanced;
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public static SimulationResponse notFired(String reason) {
        return SimulationResponse.builder()
                .wouldFire(false)
                .reasonNotFired(reason)
                .totalDebits(BigDecimal.ZERO)
                .totalCredits(BigDecimal.ZERO)
                .isBalanced(true)
                .build();
    }

    public static SimulationResponse fired(List<SimulatedEntry> entries) {
        BigDecimal totalDebits = entries.stream()
                .filter(e -> e.getEntryType() == com.financial.rules.domain.EntryType.DEBIT)
                .map(SimulatedEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredits = entries.stream()
                .filter(e -> e.getEntryType() == com.financial.rules.domain.EntryType.CREDIT)
                .map(SimulatedEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SimulationResponse.builder()
                .wouldFire(true)
                .entries(entries)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .isBalanced(totalDebits.compareTo(totalCredits) == 0)
                .build();
    }

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }
}
