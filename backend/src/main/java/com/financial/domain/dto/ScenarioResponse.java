package com.financial.domain.dto;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Scenario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioResponse {

    private Long id;
    private Long productId;
    private String productCode;
    private String code;
    private String name;
    private String description;
    private String triggerDescription;
    private String fundFlowPath;
    private EntityStatus status;
    private Integer transactionTypeCount;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static ScenarioResponse fromEntity(Scenario scenario) {
        return fromEntity(scenario, 0);
    }

    public static ScenarioResponse fromEntity(Scenario scenario, int transactionTypeCount) {
        return ScenarioResponse.builder()
                .id(scenario.getId())
                .productId(scenario.getProduct().getId())
                .productCode(scenario.getProduct().getCode())
                .code(scenario.getCode())
                .name(scenario.getName())
                .description(scenario.getDescription())
                .triggerDescription(scenario.getTriggerDescription())
                .fundFlowPath(scenario.getFundFlowPath())
                .status(scenario.getStatus())
                .transactionTypeCount(transactionTypeCount)
                .version(scenario.getVersion())
                .createdAt(scenario.getCreatedAt())
                .updatedAt(scenario.getUpdatedAt())
                .createdBy(scenario.getCreatedBy())
                .updatedBy(scenario.getUpdatedBy())
                .build();
    }
}
