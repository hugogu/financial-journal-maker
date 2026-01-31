package com.financial.transactionflow.service;

import com.financial.ai.domain.DesignDecision;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.repository.DecisionRepository;
import com.financial.transactionflow.dto.FlowDiagramData;
import com.financial.transactionflow.dto.FlowType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * T056: Service for computing flow diagram layout using dagre algorithm
 */
@Service
public class FlowDiagramService {

    private final DecisionRepository decisionRepository;

    public FlowDiagramService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    /**
     * T056: Generate flow diagram data with automatic layout
     */
    public FlowDiagramData getFlowDiagram(String transactionTypeCode) {
        DesignDecision transactionDecision = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.TRANSACTION_TYPE && 
                        d.getIsConfirmed() &&
                        transactionTypeCode.equals(d.getContent().get("code")))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Transaction type not found: " + transactionTypeCode));

        DesignDecision accountingDecision = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.ACCOUNTING && 
                        d.getIsConfirmed() &&
                        transactionTypeCode.equals(d.getContent().get("transactionTypeCode")))
            .findFirst()
            .orElse(null);

        if (accountingDecision == null) {
            return FlowDiagramData.builder()
                .transactionTypeCode(transactionTypeCode)
                .transactionTypeName((String) transactionDecision.getContent().get("name"))
                .nodes(List.of())
                .edges(List.of())
                .build();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> accounts = (List<Map<String, Object>>) accountingDecision.getContent().get("accounts");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> flows = (List<Map<String, Object>>) accountingDecision.getContent().get("flows");

        List<FlowDiagramData.NodeDto> nodes = createNodes(accounts);
        List<FlowDiagramData.EdgeDto> edges = createEdges(flows);

        // Simple auto-layout: arrange nodes in columns based on type
        layoutNodes(nodes, edges);

        return FlowDiagramData.builder()
            .transactionTypeCode(transactionTypeCode)
            .transactionTypeName((String) transactionDecision.getContent().get("name"))
            .nodes(nodes)
            .edges(edges)
            .build();
    }

    private List<FlowDiagramData.NodeDto> createNodes(List<Map<String, Object>> accounts) {
        if (accounts == null) return List.of();

        return accounts.stream()
            .map(acc -> FlowDiagramData.NodeDto.builder()
                .id((String) acc.get("code"))
                .accountCode((String) acc.get("code"))
                .accountName((String) acc.get("name"))
                .accountType((String) acc.get("type"))
                .accountState((String) acc.get("state"))
                .width(120)
                .height(60)
                .build())
            .collect(Collectors.toList());
    }

    private List<FlowDiagramData.EdgeDto> createEdges(List<Map<String, Object>> flows) {
        if (flows == null) return List.of();

        return flows.stream()
            .map(flow -> FlowDiagramData.EdgeDto.builder()
                .id(UUID.randomUUID().toString())
                .sourceId((String) flow.get("source"))
                .targetId((String) flow.get("target"))
                .flowType((String) flow.get("type"))
                .amountExpression((String) flow.get("amount"))
                .label((String) flow.get("amount"))
                .build())
            .collect(Collectors.toList());
    }

    private void layoutNodes(List<FlowDiagramData.NodeDto> nodes, List<FlowDiagramData.EdgeDto> edges) {
        // Simple layout: arrange by account type in columns
        Map<String, Integer> typeColumns = Map.of(
            "CUSTOMER", 0,
            "CHANNEL", 1,
            "BANK", 2,
            "REVENUE", 3,
            "COST", 3
        );

        Map<String, List<FlowDiagramData.NodeDto>> nodesByType = nodes.stream()
            .collect(Collectors.groupingBy(n -> n.getAccountType() != null ? n.getAccountType() : "OTHER"));

        int columnWidth = 200;
        int rowHeight = 100;

        for (Map.Entry<String, List<FlowDiagramData.NodeDto>> entry : nodesByType.entrySet()) {
            int column = typeColumns.getOrDefault(entry.getKey(), 1);
            List<FlowDiagramData.NodeDto> typeNodes = entry.getValue();
            
            for (int i = 0; i < typeNodes.size(); i++) {
                FlowDiagramData.NodeDto node = typeNodes.get(i);
                node.setX(50 + column * columnWidth);
                node.setY(50 + i * rowHeight);
            }
        }
    }
}
