package com.financial.rules.dto;

import com.financial.rules.domain.AccountingRule;
import com.financial.rules.domain.RuleStatus;
import com.financial.rules.domain.TriggerCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private RuleStatus status;
    private Boolean sharedAcrossScenarios;
    private Integer currentVersion;
    private Long version;
    private EntryTemplateResponse entryTemplate;
    private List<TriggerConditionResponse> triggerConditions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RuleResponse fromEntity(AccountingRule rule) {
        return fromEntity(rule, new ArrayList<>());
    }

    public static RuleResponse fromEntity(AccountingRule rule, List<TriggerCondition> conditions) {
        EntryTemplateResponse templateResponse = null;
        if (rule.getEntryTemplate() != null) {
            templateResponse = EntryTemplateResponse.fromEntity(rule.getEntryTemplate());
        }

        List<TriggerConditionResponse> conditionResponses = conditions != null
                ? conditions.stream().map(TriggerConditionResponse::fromEntity).toList()
                : new ArrayList<>();

        return RuleResponse.builder()
                .id(rule.getId())
                .code(rule.getCode())
                .name(rule.getName())
                .description(rule.getDescription())
                .status(rule.getStatus())
                .sharedAcrossScenarios(rule.getSharedAcrossScenarios())
                .currentVersion(rule.getCurrentVersion())
                .version(rule.getVersion())
                .entryTemplate(templateResponse)
                .triggerConditions(conditionResponses)
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
