package com.financial.transactionflow.service;

import com.financial.ai.domain.DesignDecision;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.repository.DecisionRepository;
import com.financial.transactionflow.dto.TransactionTimelineDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * T067: Service for transaction timeline operations
 */
@Service
public class TransactionTimelineService {

    private final DecisionRepository decisionRepository;

    public TransactionTimelineService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    /**
     * T067: Get timeline for a transaction type
     */
    @SuppressWarnings("unchecked")
    public TransactionTimelineDto getTransactionTimeline(String transactionTypeCode) {
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

        List<TransactionTimelineDto.TimelineEventDto> events = new ArrayList<>();
        
        if (accountingDecision != null) {
            Map<String, Object> content = accountingDecision.getContent();
            List<Map<String, Object>> entries = (List<Map<String, Object>>) content.get("entries");
            
            if (entries != null) {
                // Group by timing (settlementOffset or default to T+0)
                for (Map<String, Object> entry : entries) {
                    String timing = (String) entry.getOrDefault("settlementOffset", "T+0");
                    String trigger = (String) entry.get("triggerEvent");
                    
                    events.add(TransactionTimelineDto.TimelineEventDto.builder()
                        .timing(timing)
                        .description(trigger != null ? trigger : "Transaction processing")
                        .relatedEntryIds(List.of((String) entry.get("id")))
                        .isSettlement(timing.startsWith("T+") && !timing.equals("T+0"))
                        .build());
                }
            }
        }

        // Sort by timing
        events.sort((a, b) -> compareTiming(a.getTiming(), b.getTiming()));

        boolean hasMultipleTimings = events.stream()
            .map(TransactionTimelineDto.TimelineEventDto::getTiming)
            .distinct()
            .count() > 1;

        return TransactionTimelineDto.builder()
            .transactionTypeCode(transactionTypeCode)
            .transactionTypeName((String) transactionDecision.getContent().get("name"))
            .events(events)
            .hasMultipleTimings(hasMultipleTimings)
            .build();
    }

    private int compareTiming(String a, String b) {
        int offsetA = parseTiming(a);
        int offsetB = parseTiming(b);
        return Integer.compare(offsetA, offsetB);
    }

    private int parseTiming(String timing) {
        if (timing == null || timing.isEmpty()) return 0;
        try {
            return Integer.parseInt(timing.replace("T+", "").replace("T-", "-"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
