package com.financial.transactionflow.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * T055: DTO for flow diagram data with node positions
 */
@Data
@Builder
public class FlowDiagramData {
    
    private String transactionTypeCode;
    private String transactionTypeName;
    
    /**
     * Account nodes with positions
     */
    private List<NodeDto> nodes;
    
    /**
     * Flow connections between accounts
     */
    private List<EdgeDto> edges;
    
    @Data
    @Builder
    public static class NodeDto {
        private String id;
        private String accountCode;
        private String accountName;
        private String accountType;
        private String accountState;
        private double x;
        private double y;
        private int width;
        private int height;
    }
    
    @Data
    @Builder
    public static class EdgeDto {
        private String id;
        private String sourceId;
        private String targetId;
        private String flowType;
        private String amountExpression;
        private String label;
    }
}
