package com.financial.domain.dto;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTypeTreeNode {

    private Long id;
    private String code;
    private String name;
    private String description;
    private EntityStatus status;
    private Integer ruleCount;

    public static TransactionTypeTreeNode fromEntity(TransactionType type, int ruleCount) {
        return TransactionTypeTreeNode.builder()
                .id(type.getId())
                .code(type.getCode())
                .name(type.getName())
                .description(type.getDescription())
                .status(type.getStatus())
                .ruleCount(ruleCount)
                .build();
    }
}
