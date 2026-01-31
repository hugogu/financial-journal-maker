package com.financial.domain.dto;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Scenario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioTreeNode {

    private Long id;
    private String code;
    private String name;
    private String description;
    private EntityStatus status;
    private List<TransactionTypeTreeNode> transactionTypes;

    public static ScenarioTreeNode fromEntity(Scenario scenario, List<TransactionTypeTreeNode> transactionTypes) {
        return ScenarioTreeNode.builder()
                .id(scenario.getId())
                .code(scenario.getCode())
                .name(scenario.getName())
                .description(scenario.getDescription())
                .status(scenario.getStatus())
                .transactionTypes(transactionTypes)
                .build();
    }
}
