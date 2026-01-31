package com.financial.rules.dto;

import com.financial.rules.domain.AccountingRule;
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
public class RuleSummaryResponse {

    private Long id;
    private String code;
    private String name;
    private RuleStatus status;
    private Boolean sharedAcrossScenarios;
    private Integer entryLineCount;
    private Integer currentVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RuleSummaryResponse fromEntity(AccountingRule rule) {
        return RuleSummaryResponse.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .status(rule.getStatus())
                .sharedAcrossScenarios(rule.getSharedAcrossScenarios())
                .entryLineCount(rule.getEntryTemplate() != null ? rule.getEntryTemplate().getLines().size() : 0)
                .currentVersion(rule.getCurrentVersion())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
