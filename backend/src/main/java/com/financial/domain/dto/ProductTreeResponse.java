package com.financial.domain.dto;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTreeResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private EntityStatus status;
    private List<ScenarioTreeNode> scenarios;

    public static ProductTreeResponse fromEntity(Product product, List<ScenarioTreeNode> scenarios) {
        return ProductTreeResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .status(product.getStatus())
                .scenarios(scenarios)
                .build();
    }
}
