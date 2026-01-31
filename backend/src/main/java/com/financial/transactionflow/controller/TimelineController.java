package com.financial.transactionflow.controller;

import com.financial.transactionflow.dto.TransactionTimelineDto;
import com.financial.transactionflow.service.TransactionTimelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * T068: Controller for timeline endpoints
 */
@RestController
@RequestMapping("/api/v1/transaction-flows")
public class TimelineController {

    private final TransactionTimelineService timelineService;

    public TimelineController(TransactionTimelineService timelineService) {
        this.timelineService = timelineService;
    }

    /**
     * T068: Get timeline for a transaction type
     */
    @GetMapping("/{transactionTypeCode}/timeline")
    public ResponseEntity<TransactionTimelineDto> getTransactionTimeline(
            @PathVariable String transactionTypeCode) {
        
        TransactionTimelineDto timeline = timelineService.getTransactionTimeline(transactionTypeCode);
        return ResponseEntity.ok(timeline);
    }
}
