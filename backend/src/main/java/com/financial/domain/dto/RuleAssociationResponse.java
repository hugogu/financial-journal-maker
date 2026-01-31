package com.financial.domain.dto;

import com.financial.domain.domain.TransactionTypeRule;
import com.financial.rules.domain.RuleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleAssociationResponse {

    private Long id;
    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private RuleStatus ruleStatus;
    private Integer sequenceNumber;
    private LocalDateTime createdAt;
    private String createdBy;

    public static RuleAssociationResponse fromEntity(TransactionTypeRule ttr) {
        return RuleAssociationResponse.builder()
                .id(ttr.getId())
                .ruleId(ttr.getRule().getId())
                .ruleCode(ttr.getRule().getCode())
                .ruleName(ttr.getRule().getName())
                .ruleStatus(ttr.getRule().getStatus())
                .sequenceNumber(ttr.getSequenceNumber())
                .createdAt(ttr.getCreatedAt())
                .createdBy(ttr.getCreatedBy())
                .build();
    }
}
