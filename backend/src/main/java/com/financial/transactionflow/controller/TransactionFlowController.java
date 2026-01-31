package com.financial.transactionflow.controller;

import com.financial.transactionflow.dto.*;
import com.financial.transactionflow.service.TransactionFlowService;
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
public class TransactionFlowController {

    private final TransactionFlowService transactionFlowService;

    public TransactionFlowController(TransactionFlowService transactionFlowService) {
        this.transactionFlowService = transactionFlowService;
    }

    /**
     * T020: List all products with transaction flows
     */
    @GetMapping("/products")
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
    public ResponseEntity<ProductSummary> getProduct(@PathVariable String productCode) {
        ProductSummary product = transactionFlowService.getProduct(productCode);
        return ResponseEntity.ok(product);
    }

    /**
     * T020: List scenarios for a product
     */
    @GetMapping("/products/{productCode}/scenarios")
    public ResponseEntity<List<ScenarioSummary>> listScenarios(@PathVariable String productCode) {
        List<ScenarioSummary> scenarios = transactionFlowService.listScenarios(productCode);
        return ResponseEntity.ok(scenarios);
    }

    /**
     * T021: List all transaction flows (flat view with filters)
     */
    @GetMapping
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
    public ResponseEntity<TransactionFlowView> getTransactionFlow(
            @PathVariable String transactionTypeCode) {
        
        TransactionFlowView flow = transactionFlowService.getTransactionFlow(transactionTypeCode);
        return ResponseEntity.ok(flow);
    }
}
