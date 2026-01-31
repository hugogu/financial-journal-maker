package com.financial.transactionflow.service;

import com.financial.ai.domain.DesignDecision;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.repository.DecisionRepository;
import com.financial.transactionflow.dto.DesignPreviewDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * T044, T045: Service for real-time design preview during AI sessions
 */
@Service
public class PreviewService {

    private final DecisionRepository decisionRepository;
    
    // Store active preview streams per session
    private final Map<Long, Sinks.Many<DesignPreviewDto>> sessionSinks = new ConcurrentHashMap<>();

    public PreviewService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    /**
     * T044: Get current preview state for a session
     */
    public DesignPreviewDto getSessionPreview(Long sessionId) {
        List<DesignDecision> sessionDecisions = decisionRepository.findBySessionId(sessionId);
        
        return buildPreviewDto(sessionId, sessionDecisions);
    }

    /**
     * T045: Create SSE stream for preview updates
     */
    public Flux<DesignPreviewDto> streamSessionPreview(Long sessionId) {
        // Create or get existing sink for this session
        Sinks.Many<DesignPreviewDto> sink = sessionSinks.computeIfAbsent(sessionId, 
            id -> Sinks.many().multicast().onBackpressureBuffer());
        
        // Send initial state
        sink.tryEmitNext(getSessionPreview(sessionId));
        
        return sink.asFlux()
            .doOnCancel(() -> cleanupSink(sessionId))
            .doOnTerminate(() -> cleanupSink(sessionId));
    }

    /**
     * Notify that preview has changed for a session (called when decisions are updated)
     */
    public void notifyPreviewChanged(Long sessionId) {
        Sinks.Many<DesignPreviewDto> sink = sessionSinks.get(sessionId);
        if (sink != null) {
            DesignPreviewDto updated = getSessionPreview(sessionId);
            sink.tryEmitNext(updated);
        }
    }

    private void cleanupSink(Long sessionId) {
        sessionSinks.remove(sessionId);
    }

    @SuppressWarnings("unchecked")
    private DesignPreviewDto buildPreviewDto(Long sessionId, List<DesignDecision> decisions) {
        // Find current phase from latest decision
        Optional<DesignDecision> latestDecision = decisions.stream()
            .max((d1, d2) -> d1.getUpdatedAt().compareTo(d2.getUpdatedAt()));
        
        String currentPhase = latestDecision.map(d -> d.getDecisionType().name()).orElse("PRODUCT");
        
        // Get current transaction type if in ACCOUNTING phase
        String transactionTypeCode = null;
        String transactionTypeName = null;
        
        Optional<DesignDecision> accountingDecision = decisions.stream()
            .filter(d -> d.getDecisionType() == DesignPhase.ACCOUNTING)
            .findFirst();
        
        if (accountingDecision.isPresent()) {
            Map<String, Object> content = accountingDecision.get().getContent();
            transactionTypeCode = (String) content.get("transactionTypeCode");
        }

        // Build preview from ACCOUNTING decisions
        List<DesignPreviewDto.AccountPreviewDto> accounts = List.of();
        List<DesignPreviewDto.EntryGroupDto> entryGroups = List.of();
        int confirmedAccountCount = 0;
        int tentativeAccountCount = 0;
        
        if (accountingDecision.isPresent()) {
            Map<String, Object> content = accountingDecision.get().getContent();
            
            // Parse accounts
            List<Map<String, Object>> accountList = (List<Map<String, Object>>) content.get("accounts");
            if (accountList != null) {
                accounts = accountList.stream()
                    .map(a -> DesignPreviewDto.AccountPreviewDto.builder()
                        .accountCode((String) a.get("code"))
                        .accountName((String) a.get("name"))
                        .accountType((String) a.get("type"))
                        .confirmed(true) // From confirmed decision
                        .linkedToCoA(true)
                        .build())
                    .collect(Collectors.toList());
                confirmedAccountCount = accounts.size();
            }
            
            // Parse entries
            List<Map<String, Object>> entryList = (List<Map<String, Object>>) content.get("entries");
            if (entryList != null) {
                Map<String, List<DesignPreviewDto.EntryPreviewDto>> grouped = entryList.stream()
                    .map(e -> DesignPreviewDto.EntryPreviewDto.builder()
                        .operation((String) e.get("operation"))
                        .accountCode((String) e.get("accountCode"))
                        .accountName((String) e.get("accountName"))
                        .amountExpression((String) e.get("amountExpression"))
                        .confirmed(true)
                        .build())
                    .collect(Collectors.groupingBy(
                        e -> e.getOperation() != null ? e.getOperation() : "default",
                        Collectors.toList()
                    ));
                
                entryGroups = grouped.entrySet().stream()
                    .map(e -> DesignPreviewDto.EntryGroupDto.builder()
                        .triggerEvent(e.getKey())
                        .entries(e.getValue())
                        .build())
                    .collect(Collectors.toList());
            }
        }

        // Validate
        List<String> validationMessages = validateDesign(accounts, entryGroups);
        
        return DesignPreviewDto.builder()
            .sessionId(sessionId)
            .currentPhase(currentPhase)
            .accounts(accounts)
            .entryGroups(entryGroups)
            .transactionTypeCode(transactionTypeCode)
            .transactionTypeName(transactionTypeName)
            .confirmedAccountCount(confirmedAccountCount)
            .tentativeAccountCount(tentativeAccountCount)
            .confirmedEntryCount(entryGroups.stream().mapToInt(g -> g.getEntries().size()).sum())
            .tentativeEntryCount(0)
            .isValid(validationMessages.isEmpty())
            .validationMessages(validationMessages)
            .build();
    }

    private List<String> validateDesign(
            List<DesignPreviewDto.AccountPreviewDto> accounts,
            List<DesignPreviewDto.EntryGroupDto> entryGroups) {
        
        java.util.List<String> messages = new java.util.ArrayList<>();
        
        if (accounts.isEmpty()) {
            messages.add("No accounts defined");
        }
        
        if (entryGroups.isEmpty()) {
            messages.add("No journal entries defined");
        }
        
        // Check that all entry account codes exist in accounts
        java.util.Set<String> accountCodes = accounts.stream()
            .map(DesignPreviewDto.AccountPreviewDto::getAccountCode)
            .collect(java.util.stream.Collectors.toSet());
        
        for (DesignPreviewDto.EntryGroupDto group : entryGroups) {
            for (var entry : group.getEntries()) {
                if (!accountCodes.contains(entry.getAccountCode())) {
                    messages.add("Entry references unknown account: " + entry.getAccountCode());
                }
            }
        }
        
        return messages;
    }
}
