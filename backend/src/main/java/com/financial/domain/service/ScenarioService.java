package com.financial.domain.service;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Product;
import com.financial.domain.domain.Scenario;
import com.financial.domain.dto.ScenarioCreateRequest;
import com.financial.domain.dto.ScenarioResponse;
import com.financial.domain.dto.ScenarioUpdateRequest;
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
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ProductRepository productRepository;
    private final TransactionTypeRepository transactionTypeRepository;

    @Transactional
    public ScenarioResponse createScenario(ScenarioCreateRequest request) {
        log.info("Creating scenario with code: {} under product: {}", request.getCode(), request.getProductId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product", request.getProductId()));

        if (product.isArchived()) {
            throw new ParentArchivedException("Product", "Scenario");
        }

        if (scenarioRepository.existsByProductIdAndCode(request.getProductId(), request.getCode())) {
            throw new DuplicateCodeException("Scenario", request.getCode());
        }

        Scenario scenario = Scenario.builder()
                .product(product)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .triggerDescription(request.getTriggerDescription())
                .fundFlowPath(request.getFundFlowPath())
                .status(EntityStatus.DRAFT)
                .build();

        scenario = scenarioRepository.save(scenario);
        log.info("Created scenario with id: {}", scenario.getId());

        return ScenarioResponse.fromEntity(scenario, 0);
    }

    @Transactional(readOnly = true)
    public ScenarioResponse getScenario(Long id) {
        Scenario scenario = findScenarioById(id);
        int typeCount = transactionTypeRepository.countByScenarioId(id);
        return ScenarioResponse.fromEntity(scenario, typeCount);
    }

    @Transactional(readOnly = true)
    public Page<ScenarioResponse> listScenarios(Long productId, EntityStatus status, String search, Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        Page<Scenario> scenarios = scenarioRepository.findByFilters(productId, statusStr, search, pageable);
        return scenarios.map(s -> {
            int typeCount = transactionTypeRepository.countByScenarioId(s.getId());
            return ScenarioResponse.fromEntity(s, typeCount);
        });
    }

    @Transactional
    public ScenarioResponse updateScenario(Long id, ScenarioUpdateRequest request) {
        Scenario scenario = findScenarioById(id);

        if (!scenario.canUpdate()) {
            throw new InvalidStateTransitionException("Scenario", scenario.getStatus(), "update");
        }

        if (request.getName() != null) {
            scenario.setName(request.getName());
        }
        if (request.getDescription() != null) {
            scenario.setDescription(request.getDescription());
        }
        if (request.getTriggerDescription() != null) {
            scenario.setTriggerDescription(request.getTriggerDescription());
        }
        if (request.getFundFlowPath() != null) {
            scenario.setFundFlowPath(request.getFundFlowPath());
        }

        scenario = scenarioRepository.save(scenario);
        int typeCount = transactionTypeRepository.countByScenarioId(id);
        return ScenarioResponse.fromEntity(scenario, typeCount);
    }

    @Transactional
    public void deleteScenario(Long id) {
        Scenario scenario = findScenarioById(id);

        if (!scenario.canDelete()) {
            throw new InvalidStateTransitionException("Scenario", scenario.getStatus(), "delete");
        }

        int typeCount = transactionTypeRepository.countByScenarioId(id);
        if (typeCount > 0) {
            throw new HasChildrenException("Scenario", "transaction type", typeCount);
        }

        scenarioRepository.delete(scenario);
        log.info("Deleted scenario with id: {}", id);
    }

    @Transactional
    public ScenarioResponse activateScenario(Long id) {
        Scenario scenario = findScenarioById(id);

        if (!scenario.canActivate()) {
            throw new InvalidStateTransitionException("Scenario", scenario.getStatus(), "activate");
        }

        scenario.setStatus(EntityStatus.ACTIVE);
        scenario = scenarioRepository.save(scenario);
        int typeCount = transactionTypeRepository.countByScenarioId(id);
        log.info("Activated scenario with id: {}", id);
        return ScenarioResponse.fromEntity(scenario, typeCount);
    }

    @Transactional
    public ScenarioResponse archiveScenario(Long id) {
        Scenario scenario = findScenarioById(id);

        if (!scenario.canArchive()) {
            throw new InvalidStateTransitionException("Scenario", scenario.getStatus(), "archive");
        }

        scenario.setStatus(EntityStatus.ARCHIVED);
        scenario = scenarioRepository.save(scenario);
        int typeCount = transactionTypeRepository.countByScenarioId(id);
        log.info("Archived scenario with id: {}", id);
        return ScenarioResponse.fromEntity(scenario, typeCount);
    }

    @Transactional
    public ScenarioResponse restoreScenario(Long id) {
        Scenario scenario = findScenarioById(id);

        if (!scenario.canRestore()) {
            throw new InvalidStateTransitionException("Scenario", scenario.getStatus(), "restore");
        }

        scenario.setStatus(EntityStatus.DRAFT);
        scenario = scenarioRepository.save(scenario);
        int typeCount = transactionTypeRepository.countByScenarioId(id);
        log.info("Restored scenario with id: {}", id);
        return ScenarioResponse.fromEntity(scenario, typeCount);
    }

    private Scenario findScenarioById(Long id) {
        return scenarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scenario", id));
    }

    @Transactional
    public ScenarioResponse cloneScenario(Long id, com.financial.domain.dto.ScenarioCloneRequest request) {
        Scenario source = findScenarioById(id);
        
        Long targetProductId = request.getTargetProductId() != null 
                ? request.getTargetProductId() 
                : source.getProduct().getId();
        
        Product targetProduct = productRepository.findById(targetProductId)
                .orElseThrow(() -> new EntityNotFoundException("Product", targetProductId));
        
        if (targetProduct.isArchived()) {
            throw new ParentArchivedException("Product", "Scenario");
        }
        
        String newCode = request.getNewCode() != null ? request.getNewCode() : generateCopyCode(source.getCode(), targetProductId);
        String newName = request.getNewName() != null ? request.getNewName() : source.getName() + " (Copy)";
        
        if (scenarioRepository.existsByProductIdAndCode(targetProductId, newCode)) {
            throw new DuplicateCodeException("Scenario", newCode);
        }
        
        Scenario clone = Scenario.builder()
                .product(targetProduct)
                .code(newCode)
                .name(newName)
                .description(source.getDescription())
                .triggerDescription(source.getTriggerDescription())
                .fundFlowPath(source.getFundFlowPath())
                .status(EntityStatus.DRAFT)
                .build();
        
        clone = scenarioRepository.save(clone);
        
        java.util.List<com.financial.domain.domain.TransactionType> sourceTypes = 
                transactionTypeRepository.findByScenarioId(id);
        for (com.financial.domain.domain.TransactionType sourceType : sourceTypes) {
            cloneTransactionTypeInternal(sourceType, clone);
        }
        
        int typeCount = transactionTypeRepository.countByScenarioId(clone.getId());
        log.info("Cloned scenario {} to new scenario {}", id, clone.getId());
        return ScenarioResponse.fromEntity(clone, typeCount);
    }
    
    private void cloneTransactionTypeInternal(com.financial.domain.domain.TransactionType source, Scenario targetScenario) {
        com.financial.domain.domain.TransactionType clone = com.financial.domain.domain.TransactionType.builder()
                .scenario(targetScenario)
                .code(source.getCode())
                .name(source.getName())
                .description(source.getDescription())
                .status(EntityStatus.DRAFT)
                .build();
        
        transactionTypeRepository.save(clone);
    }
    
    private String generateCopyCode(String originalCode, Long productId) {
        String baseCode = originalCode.replaceAll("-copy-\\d+$", "");
        int copyNumber = 1;
        String newCode = baseCode + "-copy-" + copyNumber;
        
        while (scenarioRepository.existsByProductIdAndCode(productId, newCode)) {
            copyNumber++;
            newCode = baseCode + "-copy-" + copyNumber;
        }
        
        return newCode;
    }
}
