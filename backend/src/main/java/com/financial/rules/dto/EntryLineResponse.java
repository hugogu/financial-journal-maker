package com.financial.rules.dto;

import com.financial.rules.domain.EntryLine;
import com.financial.rules.domain.EntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryLineResponse {

    private Long id;
    private Integer sequenceNumber;
    private String accountCode;
    private String accountName;
    private EntryType entryType;
    private String amountExpression;
    private String memoTemplate;

    public static EntryLineResponse fromEntity(EntryLine line) {
        return EntryLineResponse.builder()
                .id(line.getId())
                .sequenceNumber(line.getSequenceNumber())
                .accountCode(line.getAccountCode())
                .entryType(line.getEntryType())
                .amountExpression(line.getAmountExpression())
                .memoTemplate(line.getMemoTemplate())
                .build();
    }

    public static EntryLineResponse fromEntity(EntryLine line, String accountName) {
        EntryLineResponse response = fromEntity(line);
        response.setAccountName(accountName);
        return response;
    }
}
