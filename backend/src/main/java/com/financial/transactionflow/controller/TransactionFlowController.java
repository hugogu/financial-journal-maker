package com.financial.transactionflow.controller;

import com.financial.transactionflow.dto.*;
import com.financial.transactionflow.service.FlowDiagramService;
import com.financial.transactionflow.service.TransactionFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for transaction flow browsing and viewing.
 * T020, T021, T029: Endpoints for products, scenarios, and transaction flows.
 */
@RestController
@RequestMapping("/api/v1/transaction-flows")
@Tag(name = "Transaction Flows", description = "Browse and view transaction flow designs")
public class TransactionFlowController {

    private final TransactionFlowService transactionFlowService;
    private final FlowDiagramService flowDiagramService;

    public TransactionFlowController(TransactionFlowService transactionFlowService,
                                      FlowDiagramService flowDiagramService) {
        this.transactionFlowService = transactionFlowService;
        this.flowDiagramService = flowDiagramService;
    }

    /**
     * T020: List all products with transaction flows
     */
    @GetMapping("/products")
    @Operation(summary = "List products", description = "Get all products with transaction flow designs")
    public ResponseEntity<Page<ProductSummary>> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ProductSummary> products = transactionFlowService.listProducts(
            search, PageRequest.of(page, size));
        return ResponseEntity.ok(products);
    }

    /**
     * T020: Get product details
     */
    @GetMapping("/products/{productCode}")
    @Operation(summary = "Get product", description = "Get product details with scenarios")
    public ResponseEntity<ProductSummary> getProduct(@PathVariable String productCode) {
        ProductSummary product = transactionFlowService.getProduct(productCode);
        return ResponseEntity.ok(product);
    }

    /**
     * T020: List scenarios for a product
     */
    @GetMapping("/products/{productCode}/scenarios")
    @Operation(summary = "List scenarios", description = "Get scenarios for a product")
    public ResponseEntity<List<ScenarioSummary>> listScenarios(@PathVariable String productCode) {
        List<ScenarioSummary> scenarios = transactionFlowService.listScenarios(productCode);
        return ResponseEntity.ok(scenarios);
    }

    /**
     * T021: List all transaction flows (flat view with filters)
     */
    @GetMapping
    @Operation(summary = "List transaction flows", description = "Get all transaction flows with optional filters")
    public ResponseEntity<Page<TransactionFlowSummary>> listAllTransactionFlows(
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String scenarioCode,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<TransactionFlowSummary> flows = transactionFlowService.listAllTransactionFlows(
            productCode, scenarioCode, search, PageRequest.of(page, size));
        return ResponseEntity.ok(flows);
    }

    /**
     * T029: Get complete transaction flow details
     */
    @GetMapping("/{transactionTypeCode}")
    @Operation(summary = "Get transaction flow", description = "Get complete transaction flow details")
    public ResponseEntity<TransactionFlowView> getTransactionFlow(
            @PathVariable String transactionTypeCode) {
        
        TransactionFlowView flow = transactionFlowService.getTransactionFlow(transactionTypeCode);
        return ResponseEntity.ok(flow);
    }

    /**
     * T038: Get Numscript for a transaction type
     */
    @GetMapping("/{transactionTypeCode}/numscript")
    @Operation(summary = "Get Numscript", description = "Get Numscript DSL code for a transaction type")
    public ResponseEntity<NumscriptViewDto> getTransactionNumscript(
            @PathVariable String transactionTypeCode) {
        
        NumscriptViewDto numscript = transactionFlowService.getNumscript(transactionTypeCode);
        return ResponseEntity.ok(numscript);
    }

    /**
     * T057: Get flow diagram data for visualization
     */
    @GetMapping("/{transactionTypeCode}/diagram")
    @Operation(summary = "Get flow diagram", description = "Get flow diagram data for visualization")
    public ResponseEntity<FlowDiagramData> getFlowDiagram(
            @PathVariable String transactionTypeCode) {
        
        FlowDiagramData diagram = flowDiagramService.getFlowDiagram(transactionTypeCode);
        return ResponseEntity.ok(diagram);
    }
}
