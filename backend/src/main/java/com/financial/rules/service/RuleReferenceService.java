package com.financial.rules.service;

import com.financial.rules.domain.AccountingRule;
import com.financial.rules.dto.RuleReferenceResponse;
import com.financial.rules.repository.AccountingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RuleReferenceService {

    private final AccountingRuleRepository ruleRepository;
    
    private final Map<Long, List<RuleReferenceResponse.ScenarioReference>> referenceStore = new ConcurrentHashMap<>();

    public void addReference(Long ruleId, String scenarioId, String scenarioName, String usageContext) {
        log.info("Adding reference from scenario {} to rule {}", scenarioId, ruleId);
        
        RuleReferenceResponse.ScenarioReference ref = RuleReferenceResponse.ScenarioReference.builder()
                .scenarioId(scenarioId)
                .scenarioName(scenarioName)
                .usageContext(usageContext)
                .build();

        referenceStore.computeIfAbsent(ruleId, k -> new ArrayList<>()).add(ref);
    }

    public void removeReference(Long ruleId, String scenarioId) {
        log.info("Removing reference from scenario {} to rule {}", scenarioId, ruleId);
        
        List<RuleReferenceResponse.ScenarioReference> refs = referenceStore.get(ruleId);
        if (refs != null) {
            refs.removeIf(r -> r.getScenarioId().equals(scenarioId));
        }
    }

    @Transactional(readOnly = true)
    public RuleReferenceResponse getReferences(Long ruleId) {
        AccountingRule rule = ruleRepository.findById(ruleId).orElse(null);
        if (rule == null) {
            return null;
        }

        List<RuleReferenceResponse.ScenarioReference> refs = referenceStore.getOrDefault(ruleId, new ArrayList<>());

        return RuleReferenceResponse.builder()
                .ruleId(ruleId)
                .ruleCode(rule.getCode())
                .isShared(rule.getSharedAcrossScenarios())
                .referenceCount(refs.size())
                .references(refs)
                .build();
    }

    public boolean hasReferences(Long ruleId) {
        List<RuleReferenceResponse.ScenarioReference> refs = referenceStore.get(ruleId);
        return refs != null && !refs.isEmpty();
    }

    public List<String> getAffectedScenarios(Long ruleId) {
        List<RuleReferenceResponse.ScenarioReference> refs = referenceStore.getOrDefault(ruleId, new ArrayList<>());
        return refs.stream()
                .map(RuleReferenceResponse.ScenarioReference::getScenarioName)
                .toList();
    }

    public ImpactAnalysis getImpactAnalysis(Long ruleId) {
        AccountingRule rule = ruleRepository.findById(ruleId).orElse(null);
        if (rule == null) {
            return new ImpactAnalysis(ruleId, false, new ArrayList<>(), "Rule not found");
        }

        List<String> affectedScenarios = getAffectedScenarios(ruleId);
        boolean hasImpact = !affectedScenarios.isEmpty();
        
        String warning = hasImpact 
                ? String.format("Modifying this rule will affect %d scenario(s): %s", 
                        affectedScenarios.size(), String.join(", ", affectedScenarios))
                : null;

        return new ImpactAnalysis(ruleId, hasImpact, affectedScenarios, warning);
    }

    public record ImpactAnalysis(
            Long ruleId,
            boolean hasImpact,
            List<String> affectedScenarios,
            String warningMessage
    ) {}
}
