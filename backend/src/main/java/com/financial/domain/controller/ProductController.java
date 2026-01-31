package com.financial.domain.controller;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.dto.*;
import com.financial.domain.service.HierarchyService;
import com.financial.domain.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management operations")
public class ProductController {

    private final ProductService productService;
    private final HierarchyService hierarchyService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + response.getId()))
                .body(response);
    }

    @GetMapping
    @Operation(summary = "List all products with optional filters")
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) EntityStatus status,
            @Parameter(description = "Search by code or name")
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.listProducts(status, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get product by code")
    public ResponseEntity<ProductResponse> getProductByCode(
            @PathVariable String code) {
        return ResponseEntity.ok(productService.getProductByCode(code));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate product")
    public ResponseEntity<ProductResponse> activateProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.activateProduct(id));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive product")
    public ResponseEntity<ProductResponse> archiveProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.archiveProduct(id));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore archived product to draft")
    public ResponseEntity<ProductResponse> restoreProduct(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.restoreProduct(id));
    }

    @GetMapping("/{id}/tree")
    @Operation(summary = "Get product hierarchy tree with all scenarios and transaction types")
    public ResponseEntity<ProductTreeResponse> getProductTree(
            @PathVariable Long id) {
        return ResponseEntity.ok(hierarchyService.getProductTree(id));
    }

    @GetMapping("/{id}/rules")
    @Operation(summary = "Get all rules associated with this product through its transaction types")
    public ResponseEntity<java.util.List<RuleSummary>> getProductRules(
            @PathVariable Long id) {
        return ResponseEntity.ok(hierarchyService.getProductRules(id));
    }

    @GetMapping("/{id}/accounts")
    @Operation(summary = "Get all accounts used in rules associated with this product")
    public ResponseEntity<java.util.List<AccountSummary>> getProductAccounts(
            @PathVariable Long id) {
        return ResponseEntity.ok(hierarchyService.getProductAccounts(id));
    }

    @PostMapping("/{id}/clone")
    @Operation(summary = "Clone product with all scenarios and transaction types (without rule associations)")
    public ResponseEntity<ProductResponse> cloneProduct(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) CloneRequest request) {
        CloneRequest cloneRequest = request != null ? request : new CloneRequest();
        ProductResponse response = productService.cloneProduct(id, cloneRequest);
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + response.getId()))
                .body(response);
    }
}
