package com.financial.rules.dto;

import com.financial.rules.domain.EntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulatedEntry {

    private String accountCode;
    private String accountName;
    private EntryType entryType;
    private BigDecimal amount;
    private String currency;
    private String memo;
}
