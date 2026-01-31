package com.financial.domain.service;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Product;
import com.financial.domain.dto.ProductCreateRequest;
import com.financial.domain.dto.ProductResponse;
import com.financial.domain.dto.ProductUpdateRequest;
import com.financial.domain.exception.*;
import com.financial.domain.repository.ProductRepository;
import com.financial.domain.repository.ScenarioRepository;
import com.financial.domain.repository.TransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ScenarioRepository scenarioRepository;
    private final TransactionTypeRepository transactionTypeRepository;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product with code: {}", request.getCode());

        if (productRepository.existsByCode(request.getCode())) {
            throw new DuplicateCodeException("Product", request.getCode());
        }

        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .businessModel(request.getBusinessModel())
                .participants(request.getParticipants())
                .fundFlow(request.getFundFlow())
                .status(EntityStatus.DRAFT)
                .build();

        product = productRepository.save(product);
        log.info("Created product with id: {}", product.getId());

        return ProductResponse.fromEntity(product, 0);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = findProductById(id);
        int scenarioCount = scenarioRepository.countByProductId(id);
        return ProductResponse.fromEntity(product, scenarioCount);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByCode(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Product", code));
        int scenarioCount = scenarioRepository.countByProductId(product.getId());
        return ProductResponse.fromEntity(product, scenarioCount);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listProducts(EntityStatus status, String search, Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        Page<Product> products = productRepository.findByFilters(statusStr, search, pageable);
        return products.map(p -> {
            int scenarioCount = scenarioRepository.countByProductId(p.getId());
            return ProductResponse.fromEntity(p, scenarioCount);
        });
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findProductById(id);

        if (!product.canUpdate()) {
            throw new InvalidStateTransitionException("Product", product.getStatus(), "update");
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getBusinessModel() != null) {
            product.setBusinessModel(request.getBusinessModel());
        }
        if (request.getParticipants() != null) {
            product.setParticipants(request.getParticipants());
        }
        if (request.getFundFlow() != null) {
            product.setFundFlow(request.getFundFlow());
        }

        product = productRepository.save(product);
        int scenarioCount = scenarioRepository.countByProductId(id);
        return ProductResponse.fromEntity(product, scenarioCount);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductById(id);

        if (!product.canDelete()) {
            throw new InvalidStateTransitionException("Product", product.getStatus(), "delete");
        }

        int scenarioCount = scenarioRepository.countByProductId(id);
        if (scenarioCount > 0) {
            throw new HasChildrenException("Product", "scenario", scenarioCount);
        }

        productRepository.delete(product);
        log.info("Deleted product with id: {}", id);
    }

    @Transactional
    public ProductResponse activateProduct(Long id) {
        Product product = findProductById(id);

        if (!product.canActivate()) {
            throw new InvalidStateTransitionException("Product", product.getStatus(), "activate");
        }

        product.setStatus(EntityStatus.ACTIVE);
        product = productRepository.save(product);
        int scenarioCount = scenarioRepository.countByProductId(id);
        log.info("Activated product with id: {}", id);
        return ProductResponse.fromEntity(product, scenarioCount);
    }

    @Transactional
    public ProductResponse archiveProduct(Long id) {
        Product product = findProductById(id);

        if (!product.canArchive()) {
            throw new InvalidStateTransitionException("Product", product.getStatus(), "archive");
        }

        product.setStatus(EntityStatus.ARCHIVED);
        product = productRepository.save(product);
        int scenarioCount = scenarioRepository.countByProductId(id);
        log.info("Archived product with id: {}", id);
        return ProductResponse.fromEntity(product, scenarioCount);
    }

    @Transactional
    public ProductResponse restoreProduct(Long id) {
        Product product = findProductById(id);

        if (!product.canRestore()) {
            throw new InvalidStateTransitionException("Product", product.getStatus(), "restore");
        }

        product.setStatus(EntityStatus.DRAFT);
        product = productRepository.save(product);
        int scenarioCount = scenarioRepository.countByProductId(id);
        log.info("Restored product with id: {}", id);
        return ProductResponse.fromEntity(product, scenarioCount);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product", id));
    }

    @Transactional
    public ProductResponse cloneProduct(Long id, com.financial.domain.dto.CloneRequest request) {
        Product source = findProductById(id);
        
        String newCode = request.getNewCode() != null ? request.getNewCode() : generateCopyCode(source.getCode());
        String newName = request.getNewName() != null ? request.getNewName() : source.getName() + " (Copy)";
        
        if (productRepository.existsByCode(newCode)) {
            throw new DuplicateCodeException("Product", newCode);
        }
        
        Product clone = Product.builder()
                .code(newCode)
                .name(newName)
                .description(source.getDescription())
                .businessModel(source.getBusinessModel())
                .participants(source.getParticipants())
                .fundFlow(source.getFundFlow())
                .status(EntityStatus.DRAFT)
                .build();
        
        clone = productRepository.save(clone);
        
        java.util.List<com.financial.domain.domain.Scenario> sourceScenarios = scenarioRepository.findByProductId(id);
        for (com.financial.domain.domain.Scenario sourceScenario : sourceScenarios) {
            cloneScenarioInternal(sourceScenario, clone);
        }
        
        int scenarioCount = scenarioRepository.countByProductId(clone.getId());
        log.info("Cloned product {} to new product {}", id, clone.getId());
        return ProductResponse.fromEntity(clone, scenarioCount);
    }
    
    private void cloneScenarioInternal(com.financial.domain.domain.Scenario source, Product targetProduct) {
        com.financial.domain.domain.Scenario clone = com.financial.domain.domain.Scenario.builder()
                .product(targetProduct)
                .code(source.getCode())
                .name(source.getName())
                .description(source.getDescription())
                .triggerDescription(source.getTriggerDescription())
                .fundFlowPath(source.getFundFlowPath())
                .status(EntityStatus.DRAFT)
                .build();
        
        clone = scenarioRepository.save(clone);
        
        java.util.List<com.financial.domain.domain.TransactionType> sourceTypes = 
                transactionTypeRepository.findByScenarioId(source.getId());
        for (com.financial.domain.domain.TransactionType sourceType : sourceTypes) {
            cloneTransactionTypeInternal(sourceType, clone);
        }
    }
    
    private void cloneTransactionTypeInternal(com.financial.domain.domain.TransactionType source, 
                                               com.financial.domain.domain.Scenario targetScenario) {
        com.financial.domain.domain.TransactionType clone = com.financial.domain.domain.TransactionType.builder()
                .scenario(targetScenario)
                .code(source.getCode())
                .name(source.getName())
                .description(source.getDescription())
                .status(EntityStatus.DRAFT)
                .build();
        
        transactionTypeRepository.save(clone);
    }
    
    private String generateCopyCode(String originalCode) {
        String baseCode = originalCode.replaceAll("-copy-\\d+$", "");
        int copyNumber = 1;
        String newCode = baseCode + "-copy-" + copyNumber;
        
        while (productRepository.existsByCode(newCode)) {
            copyNumber++;
            newCode = baseCode + "-copy-" + copyNumber;
        }
        
        return newCode;
    }
}
