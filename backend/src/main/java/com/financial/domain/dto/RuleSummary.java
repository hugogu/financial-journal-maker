package com.financial.domain.dto;

import com.financial.rules.domain.AccountingRule;
import com.financial.rules.domain.RuleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleSummary {

    private Long id;
    private String code;
    private String name;
    private String description;
    private RuleStatus status;
    private Boolean sharedAcrossScenarios;

    public static RuleSummary fromEntity(AccountingRule rule) {
        return RuleSummary.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .description(rule.getDescription())
                .status(rule.getStatus())
                .sharedAcrossScenarios(rule.getSharedAcrossScenarios())
                .build();
    }
}
