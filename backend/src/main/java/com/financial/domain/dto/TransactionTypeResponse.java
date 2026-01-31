package com.financial.domain.dto;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTypeResponse {

    private Long id;
    private Long scenarioId;
    private String scenarioCode;
    private Long productId;
    private String productCode;
    private String code;
    private String name;
    private String description;
    private EntityStatus status;
    private Integer ruleCount;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static TransactionTypeResponse fromEntity(TransactionType type) {
        return fromEntity(type, 0);
    }

    public static TransactionTypeResponse fromEntity(TransactionType type, int ruleCount) {
        return TransactionTypeResponse.builder()
                .id(type.getId())
                .scenarioId(type.getScenario().getId())
                .scenarioCode(type.getScenario().getCode())
                .productId(type.getScenario().getProduct().getId())
                .productCode(type.getScenario().getProduct().getCode())
                .code(type.getCode())
                .name(type.getName())
                .description(type.getDescription())
                .status(type.getStatus())
                .ruleCount(ruleCount)
                .version(type.getVersion())
                .createdAt(type.getCreatedAt())
                .updatedAt(type.getUpdatedAt())
                .createdBy(type.getCreatedBy())
                .updatedBy(type.getUpdatedBy())
                .build();
    }
}
