package com.financial.domain.dto;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String businessModel;
    private String participants;
    private String fundFlow;
    private EntityStatus status;
    private Integer scenarioCount;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static ProductResponse fromEntity(Product product) {
        return fromEntity(product, 0);
    }

    public static ProductResponse fromEntity(Product product, int scenarioCount) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .businessModel(product.getBusinessModel())
                .participants(product.getParticipants())
                .fundFlow(product.getFundFlow())
                .status(product.getStatus())
                .scenarioCount(scenarioCount)
                .version(product.getVersion())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }
}
