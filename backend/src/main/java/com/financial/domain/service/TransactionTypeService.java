package com.financial.domain.service;

import com.financial.domain.domain.EntityStatus;
import com.financial.domain.domain.Scenario;
import com.financial.domain.domain.TransactionType;
import com.financial.domain.domain.TransactionTypeRule;
import com.financial.domain.dto.*;
import com.financial.domain.exception.*;
import com.financial.domain.repository.ScenarioRepository;
import com.financial.domain.repository.TransactionTypeRepository;
import com.financial.domain.repository.TransactionTypeRuleRepository;
import com.financial.rules.domain.AccountingRule;
import com.financial.rules.domain.RuleStatus;
import com.financial.rules.repository.AccountingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;
    private final ScenarioRepository scenarioRepository;
    private final TransactionTypeRuleRepository transactionTypeRuleRepository;
    private final AccountingRuleRepository accountingRuleRepository;

    @Transactional
    public TransactionTypeResponse createTransactionType(TransactionTypeCreateRequest request) {
        log.info("Creating transaction type with code: {} under scenario: {}", request.getCode(), request.getScenarioId());

        Scenario scenario = scenarioRepository.findById(request.getScenarioId())
                .orElseThrow(() -> new EntityNotFoundException("Scenario", request.getScenarioId()));

        if (scenario.isArchived()) {
            throw new ParentArchivedException("Scenario", "TransactionType");
        }

        if (transactionTypeRepository.existsByScenarioIdAndCode(request.getScenarioId(), request.getCode())) {
            throw new DuplicateCodeException("TransactionType", request.getCode());
        }

        TransactionType type = TransactionType.builder()
                .scenario(scenario)
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .status(EntityStatus.DRAFT)
                .build();

        type = transactionTypeRepository.save(type);
        log.info("Created transaction type with id: {}", type.getId());

        return TransactionTypeResponse.fromEntity(type, 0);
    }

    @Transactional(readOnly = true)
    public TransactionTypeResponse getTransactionType(Long id) {
        TransactionType type = findTransactionTypeById(id);
        int ruleCount = transactionTypeRuleRepository.countByTransactionTypeId(id);
        return TransactionTypeResponse.fromEntity(type, ruleCount);
    }

    @Transactional(readOnly = true)
    public Page<TransactionTypeResponse> listTransactionTypes(Long scenarioId, EntityStatus status, String search, Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        Page<TransactionType> types = transactionTypeRepository.findByFilters(scenarioId, statusStr, search, pageable);
        return types.map(t -> {
            int ruleCount = transactionTypeRuleRepository.countByTransactionTypeId(t.getId());
            return TransactionTypeResponse.fromEntity(t, ruleCount);
        });
    }

    @Transactional
    public TransactionTypeResponse updateTransactionType(Long id, TransactionTypeUpdateRequest request) {
        TransactionType type = findTransactionTypeById(id);

        if (!type.canUpdate()) {
            throw new InvalidStateTransitionException("TransactionType", type.getStatus(), "update");
        }

        if (request.getName() != null) {
            type.setName(request.getName());
        }
        if (request.getDescription() != null) {
            type.setDescription(request.getDescription());
        }

        type = transactionTypeRepository.save(type);
        int ruleCount = transactionTypeRuleRepository.countByTransactionTypeId(id);
        return TransactionTypeResponse.fromEntity(type, ruleCount);
    }

    @Transactional
    public void deleteTransactionType(Long id) {
        TransactionType type = findTransactionTypeById(id);

        if (!type.canDelete()) {
            throw new InvalidStateTransitionException("TransactionType", type.getStatus(), "delete");
        }

        transactionTypeRuleRepository.deleteByTransactionTypeId(id);
        transactionTypeRepository.delete(type);
        log.info("Deleted transaction type with id: {}", id);
    }

    @Transactional
    public TransactionTypeResponse activateTransactionType(Long id) {
        TransactionType type = findTransactionTypeById(id);

        if (!type.canActivate()) {
            throw new InvalidStateTransitionException("TransactionType", type.getStatus(), "activate");
        }

        type.setStatus(EntityStatus.ACTIVE);
        type = transactionTypeRepository.save(type);
        int ruleCount = transactionTypeRuleRepository.countByTransactionTypeId(id);
        log.info("Activated transaction type with id: {}", id);
        return TransactionTypeResponse.fromEntity(type, ruleCount);
    }

    @Transactional
    public TransactionTypeResponse archiveTransactionType(Long id) {
        TransactionType type = findTransactionTypeById(id);

        if (!type.canArchive()) {
            throw new InvalidStateTransitionException("TransactionType", type.getStatus(), "archive");
        }

        type.setStatus(EntityStatus.ARCHIVED);
        type = transactionTypeRepository.save(type);
        int ruleCount = transactionTypeRuleRepository.countByTransactionTypeId(id);
        log.info("Archived transaction type with id: {}", id);
        return TransactionTypeResponse.fromEntity(type, ruleCount);
    }

    @Transactional
    public TransactionTypeResponse restoreTransactionType(Long id) {
        TransactionType type = findTransactionTypeById(id);

        if (!type.canRestore()) {
            throw new InvalidStateTransitionException("TransactionType", type.getStatus(), "restore");
        }

        type.setStatus(EntityStatus.DRAFT);
        type = transactionTypeRepository.save(type);
        int ruleCount = transactionTypeRuleRepository.countByTransactionTypeId(id);
        log.info("Restored transaction type with id: {}", id);
        return TransactionTypeResponse.fromEntity(type, ruleCount);
    }

    @Transactional
    public RuleAssociationResponse addRuleAssociation(Long transactionTypeId, RuleAssociationRequest request) {
        TransactionType type = findTransactionTypeById(transactionTypeId);

        AccountingRule rule = accountingRuleRepository.findById(request.getRuleId())
                .orElseThrow(() -> new EntityNotFoundException("AccountingRule", request.getRuleId()));

        if (rule.getStatus() == RuleStatus.ARCHIVED) {
            throw new DomainException("Cannot associate an archived rule");
        }

        if (transactionTypeRuleRepository.existsByTransactionTypeIdAndRuleId(transactionTypeId, request.getRuleId())) {
            throw new DomainException("Rule is already associated with this transaction type");
        }

        int sequenceNumber = request.getSequenceNumber() != null 
                ? request.getSequenceNumber() 
                : transactionTypeRuleRepository.countByTransactionTypeId(transactionTypeId);

        TransactionTypeRule association = TransactionTypeRule.builder()
                .transactionType(type)
                .rule(rule)
                .sequenceNumber(sequenceNumber)
                .build();

        association = transactionTypeRuleRepository.save(association);
        log.info("Added rule {} to transaction type {}", request.getRuleId(), transactionTypeId);

        return RuleAssociationResponse.fromEntity(association);
    }

    @Transactional
    public void removeRuleAssociation(Long transactionTypeId, Long ruleId) {
        TransactionTypeRule association = transactionTypeRuleRepository
                .findByTransactionTypeIdAndRuleId(transactionTypeId, ruleId)
                .orElseThrow(() -> new EntityNotFoundException("RuleAssociation", 
                        "transactionTypeId=" + transactionTypeId + ", ruleId=" + ruleId));

        transactionTypeRuleRepository.delete(association);
        log.info("Removed rule {} from transaction type {}", ruleId, transactionTypeId);
    }

    @Transactional(readOnly = true)
    public List<RuleAssociationResponse> getRuleAssociations(Long transactionTypeId) {
        findTransactionTypeById(transactionTypeId);
        
        return transactionTypeRuleRepository.findByTransactionTypeIdOrderBySequenceNumberAsc(transactionTypeId)
                .stream()
                .map(RuleAssociationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private TransactionType findTransactionTypeById(Long id) {
        return transactionTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TransactionType", id));
    }
}
