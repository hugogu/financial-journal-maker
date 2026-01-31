package com.financial.rules.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.rules.domain.*;
import com.financial.rules.dto.*;
import com.financial.rules.exception.*;
import com.financial.rules.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountingRuleService {

    private final AccountingRuleRepository ruleRepository;
    private final AccountingRuleVersionRepository versionRepository;
    private final EntryTemplateRepository templateRepository;
    private final EntryLineRepository lineRepository;
    private final ExpressionParser expressionParser;
    private final ObjectMapper objectMapper;

    public RuleResponse createRule(RuleCreateRequest request) {
        log.info("Creating accounting rule with code: {}", request.getCode());

        if (ruleRepository.existsByCode(request.getCode())) {
            throw new RuleValidationException("code", "Rule code already exists: " + request.getCode());
        }

        AccountingRule rule = AccountingRule.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .sharedAcrossScenarios(request.getSharedAcrossScenarios() != null ? request.getSharedAcrossScenarios() : false)
                .status(RuleStatus.DRAFT)
                .currentVersion(1)
                .build();

        rule = ruleRepository.save(rule);

        EntryTemplate template = createEntryTemplate(rule, request.getEntryTemplate());
        rule.setEntryTemplate(template);

        List<TriggerCondition> conditions = new ArrayList<>();
        if (request.getTriggerConditions() != null) {
            conditions = saveTriggerConditions(rule.getId(), request.getTriggerConditions());
        }

        createVersion(rule, "Initial version");

        log.info("Created accounting rule with id: {}", rule.getId());
        return RuleResponse.fromEntity(rule, conditions);
    }

    @Transactional(readOnly = true)
    public RuleResponse getRule(Long id) {
        AccountingRule rule = findRuleById(id);
        List<TriggerCondition> conditions = getTriggerConditions(id);
        return RuleResponse.fromEntity(rule, conditions);
    }

    @Transactional(readOnly = true)
    public RuleResponse getRuleByCode(String code) {
        AccountingRule rule = ruleRepository.findByCode(code)
                .orElseThrow(() -> new RuleNotFoundException(code));
        List<TriggerCondition> conditions = getTriggerConditions(rule.getId());
        return RuleResponse.fromEntity(rule, conditions);
    }

    public RuleResponse updateRule(Long id, RuleUpdateRequest request) {
        log.info("Updating accounting rule with id: {}", id);

        AccountingRule rule = findRuleById(id);

        if (!rule.canUpdate()) {
            throw new InvalidStateTransitionException(rule.getStatus().name(), "UPDATE",
                    "Only DRAFT rules can be updated");
        }

        if (!rule.getVersion().equals(request.getVersion())) {
            throw new RuleValidationException("version",
                    String.format("Version mismatch. Current version is %d, but you provided %d",
                            rule.getVersion(), request.getVersion()));
        }

        if (request.getName() != null) {
            rule.setName(request.getName());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        if (request.getSharedAcrossScenarios() != null) {
            rule.setSharedAcrossScenarios(request.getSharedAcrossScenarios());
        }

        if (request.getEntryTemplate() != null) {
            updateEntryTemplate(rule, request.getEntryTemplate());
        }

        List<TriggerCondition> conditions = getTriggerConditions(id);
        if (request.getTriggerConditions() != null) {
            deleteTriggerConditions(id);
            conditions = saveTriggerConditions(id, request.getTriggerConditions());
        }

        rule.setCurrentVersion(rule.getCurrentVersion() + 1);
        rule = ruleRepository.save(rule);

        createVersion(rule, "Updated rule");

        log.info("Updated accounting rule with id: {}", id);
        return RuleResponse.fromEntity(rule, conditions);
    }

    public void deleteRule(Long id) {
        log.info("Deleting accounting rule with id: {}", id);

        AccountingRule rule = findRuleById(id);

        if (!rule.canDelete()) {
            throw new InvalidStateTransitionException(rule.getStatus().name(), "DELETE",
                    "Cannot delete ACTIVE rules. Archive first.");
        }

        ruleRepository.delete(rule);
        log.info("Deleted accounting rule with id: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<RuleSummaryResponse> listRules(RuleStatus status, Boolean shared, String search, Pageable pageable) {
        Page<AccountingRule> rules = ruleRepository.findByFilters(status, shared, search, pageable);
        return rules.map(RuleSummaryResponse::fromEntity);
    }

    public RuleResponse cloneRule(Long id, String newCode, String newName) {
        log.info("Cloning accounting rule {} to new code: {}", id, newCode);

        if (ruleRepository.existsByCode(newCode)) {
            throw new RuleValidationException("code", "Rule code already exists: " + newCode);
        }

        AccountingRule source = findRuleById(id);
        List<TriggerCondition> sourceConditions = getTriggerConditions(id);

        AccountingRule clone = AccountingRule.builder()
                .code(newCode)
                .name(newName)
                .description(source.getDescription())
                .sharedAcrossScenarios(false)
                .status(RuleStatus.DRAFT)
                .currentVersion(1)
                .build();

        clone = ruleRepository.save(clone);

        if (source.getEntryTemplate() != null) {
            cloneEntryTemplate(source.getEntryTemplate(), clone);
        }

        List<TriggerCondition> clonedConditions = new ArrayList<>();
        for (TriggerCondition condition : sourceConditions) {
            TriggerCondition cloned = TriggerCondition.builder()
                    .ruleId(clone.getId())
                    .conditionJson(condition.getConditionJson())
                    .description(condition.getDescription())
                    .build();
            clonedConditions.add(cloned);
        }
        saveTriggerConditionsEntities(clonedConditions);

        createVersion(clone, "Cloned from rule: " + source.getCode());

        log.info("Cloned accounting rule to id: {}", clone.getId());
        return RuleResponse.fromEntity(clone, clonedConditions);
    }

    public RuleResponse activateRule(Long id) {
        log.info("Activating accounting rule with id: {}", id);

        AccountingRule rule = findRuleById(id);

        if (!rule.canActivate()) {
            throw new InvalidStateTransitionException(rule.getStatus().name(), RuleStatus.ACTIVE.name(),
                    "Only DRAFT rules can be activated");
        }

        validateRuleForActivation(rule);

        rule.setStatus(RuleStatus.ACTIVE);
        rule.setCurrentVersion(rule.getCurrentVersion() + 1);
        rule = ruleRepository.save(rule);

        createVersion(rule, "Activated rule");

        List<TriggerCondition> conditions = getTriggerConditions(id);
        log.info("Activated accounting rule with id: {}", id);
        return RuleResponse.fromEntity(rule, conditions);
    }

    public RuleResponse archiveRule(Long id) {
        log.info("Archiving accounting rule with id: {}", id);

        AccountingRule rule = findRuleById(id);

        if (!rule.canArchive()) {
            throw new InvalidStateTransitionException(rule.getStatus().name(), RuleStatus.ARCHIVED.name());
        }

        rule.setStatus(RuleStatus.ARCHIVED);
        rule.setCurrentVersion(rule.getCurrentVersion() + 1);
        rule = ruleRepository.save(rule);

        createVersion(rule, "Archived rule");

        List<TriggerCondition> conditions = getTriggerConditions(id);
        log.info("Archived accounting rule with id: {}", id);
        return RuleResponse.fromEntity(rule, conditions);
    }

    public RuleResponse restoreRule(Long id) {
        log.info("Restoring accounting rule with id: {}", id);

        AccountingRule rule = findRuleById(id);

        if (!rule.canRestore()) {
            throw new InvalidStateTransitionException(rule.getStatus().name(), RuleStatus.DRAFT.name(),
                    "Only ARCHIVED rules can be restored");
        }

        rule.setStatus(RuleStatus.DRAFT);
        rule.setCurrentVersion(rule.getCurrentVersion() + 1);
        rule = ruleRepository.save(rule);

        createVersion(rule, "Restored to draft");

        List<TriggerCondition> conditions = getTriggerConditions(id);
        log.info("Restored accounting rule with id: {}", id);
        return RuleResponse.fromEntity(rule, conditions);
    }

    @Transactional(readOnly = true)
    public Page<VersionSummaryResponse> listVersions(Long ruleId, Pageable pageable) {
        findRuleById(ruleId);
        Page<AccountingRuleVersion> versions = versionRepository.findByRuleIdOrderByVersionNumberDesc(ruleId, pageable);
        return versions.map(VersionSummaryResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public VersionResponse getVersion(Long ruleId, Integer versionNumber) {
        findRuleById(ruleId);
        AccountingRuleVersion version = versionRepository.findByRuleIdAndVersionNumber(ruleId, versionNumber)
                .orElseThrow(() -> new RuleNotFoundException(ruleId));
        return VersionResponse.fromEntity(version);
    }

    public RuleResponse rollbackToVersion(Long ruleId, Integer versionNumber) {
        log.info("Rolling back rule {} to version {}", ruleId, versionNumber);

        AccountingRule rule = findRuleById(ruleId);

        if (rule.isActive()) {
            throw new InvalidStateTransitionException(rule.getStatus().name(), "ROLLBACK",
                    "Cannot rollback ACTIVE rules. Archive first.");
        }

        AccountingRuleVersion version = versionRepository.findByRuleIdAndVersionNumber(ruleId, versionNumber)
                .orElseThrow(() -> new RuleNotFoundException(ruleId));

        try {
            RuleSnapshot snapshot = objectMapper.readValue(version.getSnapshotJson(), RuleSnapshot.class);

            rule.setName(snapshot.name());
            rule.setDescription(snapshot.description());
            rule.setStatus(RuleStatus.DRAFT);
            rule.setCurrentVersion(rule.getCurrentVersion() + 1);
            rule = ruleRepository.save(rule);

            createVersion(rule, "Rolled back to version " + versionNumber);

        } catch (JsonProcessingException e) {
            throw new RulesException("Failed to parse version snapshot", e);
        }

        List<TriggerCondition> conditions = getTriggerConditions(ruleId);
        log.info("Rolled back rule {} to version {}", ruleId, versionNumber);
        return RuleResponse.fromEntity(rule, conditions);
    }

    private AccountingRule findRuleById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));
    }

    private EntryTemplate createEntryTemplate(AccountingRule rule, EntryTemplateRequest request) {
        String schemaJson = "[]";
        if (request.getVariableSchema() != null) {
            try {
                schemaJson = objectMapper.writeValueAsString(request.getVariableSchema());
            } catch (JsonProcessingException e) {
                throw new RulesException("Failed to serialize variable schema", e);
            }
        }

        EntryTemplate template = EntryTemplate.builder()
                .rule(rule)
                .description(request.getDescription())
                .variableSchemaJson(schemaJson)
                .build();

        template = templateRepository.save(template);

        int sequence = 1;
        for (EntryLineRequest lineRequest : request.getLines()) {
            validateExpression(lineRequest.getAmountExpression(), request.getVariableSchema());

            EntryLine line = EntryLine.builder()
                    .template(template)
                    .sequenceNumber(sequence++)
                    .accountCode(lineRequest.getAccountCode())
                    .entryType(lineRequest.getEntryType())
                    .amountExpression(lineRequest.getAmountExpression())
                    .memoTemplate(lineRequest.getMemoTemplate())
                    .build();

            template.addLine(line);
        }

        return templateRepository.save(template);
    }

    private void updateEntryTemplate(AccountingRule rule, EntryTemplateRequest request) {
        EntryTemplate template = rule.getEntryTemplate();
        
        if (template == null) {
            template = createEntryTemplate(rule, request);
            rule.setEntryTemplate(template);
            return;
        }

        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }

        if (request.getVariableSchema() != null) {
            try {
                template.setVariableSchemaJson(objectMapper.writeValueAsString(request.getVariableSchema()));
            } catch (JsonProcessingException e) {
                throw new RulesException("Failed to serialize variable schema", e);
            }
        }

        if (request.getLines() != null) {
            template.clearLines();
            lineRepository.deleteByTemplateId(template.getId());

            int sequence = 1;
            for (EntryLineRequest lineRequest : request.getLines()) {
                validateExpression(lineRequest.getAmountExpression(), request.getVariableSchema());

                EntryLine line = EntryLine.builder()
                        .template(template)
                        .sequenceNumber(sequence++)
                        .accountCode(lineRequest.getAccountCode())
                        .entryType(lineRequest.getEntryType())
                        .amountExpression(lineRequest.getAmountExpression())
                        .memoTemplate(lineRequest.getMemoTemplate())
                        .build();

                template.addLine(line);
            }
        }

        templateRepository.save(template);
    }

    private void cloneEntryTemplate(EntryTemplate source, AccountingRule targetRule) {
        EntryTemplate clone = EntryTemplate.builder()
                .rule(targetRule)
                .description(source.getDescription())
                .variableSchemaJson(source.getVariableSchemaJson())
                .build();

        clone = templateRepository.save(clone);

        for (EntryLine line : source.getLines()) {
            EntryLine clonedLine = EntryLine.builder()
                    .template(clone)
                    .sequenceNumber(line.getSequenceNumber())
                    .accountCode(line.getAccountCode())
                    .entryType(line.getEntryType())
                    .amountExpression(line.getAmountExpression())
                    .memoTemplate(line.getMemoTemplate())
                    .build();

            clone.addLine(clonedLine);
        }

        templateRepository.save(clone);
        targetRule.setEntryTemplate(clone);
    }

    private void validateExpression(String expression, List<VariableDefinition> schema) {
        ExpressionParser.ValidationResult result = expressionParser.validate(expression, schema);
        if (!result.valid()) {
            throw new ExpressionParseException(
                    "Invalid expression: " + String.join(", ", result.errors()),
                    expression);
        }
    }

    private void validateRuleForActivation(AccountingRule rule) {
        List<RuleValidationException.ValidationError> errors = new ArrayList<>();

        if (rule.getEntryTemplate() == null || rule.getEntryTemplate().getLines().isEmpty()) {
            errors.add(new RuleValidationException.ValidationError(
                    "entryTemplate", "Rule must have at least one entry line", null));
        }

        if (!errors.isEmpty()) {
            throw new RuleValidationException("Rule validation failed for activation", errors);
        }
    }

    private List<TriggerCondition> getTriggerConditions(Long ruleId) {
        return new ArrayList<>();
    }

    private List<TriggerCondition> saveTriggerConditions(Long ruleId, List<TriggerConditionRequest> requests) {
        List<TriggerCondition> conditions = new ArrayList<>();
        for (TriggerConditionRequest request : requests) {
            try {
                String json = objectMapper.writeValueAsString(request.getConditionJson());
                TriggerCondition condition = TriggerCondition.builder()
                        .ruleId(ruleId)
                        .conditionJson(json)
                        .description(request.getDescription())
                        .build();
                conditions.add(condition);
            } catch (JsonProcessingException e) {
                throw new RulesException("Failed to serialize condition JSON", e);
            }
        }
        return conditions;
    }

    private void saveTriggerConditionsEntities(List<TriggerCondition> conditions) {
    }

    private void deleteTriggerConditions(Long ruleId) {
    }

    private void createVersion(AccountingRule rule, String changeDescription) {
        try {
            RuleSnapshot snapshot = new RuleSnapshot(
                    rule.getCode(),
                    rule.getName(),
                    rule.getDescription(),
                    rule.getStatus().name()
            );
            String snapshotJson = objectMapper.writeValueAsString(snapshot);

            AccountingRuleVersion version = AccountingRuleVersion.builder()
                    .ruleId(rule.getId())
                    .versionNumber(rule.getCurrentVersion())
                    .snapshotJson(snapshotJson)
                    .changeDescription(changeDescription)
                    .build();

            versionRepository.save(version);
        } catch (JsonProcessingException e) {
            throw new RulesException("Failed to create version snapshot", e);
        }
    }

    private record RuleSnapshot(String code, String name, String description, String status) {}
}
