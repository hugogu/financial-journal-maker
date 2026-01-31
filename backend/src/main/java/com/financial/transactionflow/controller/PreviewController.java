package com.financial.transactionflow.controller;

import com.financial.transactionflow.dto.DesignPreviewDto;
import com.financial.transactionflow.service.PreviewService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * T046: Controller for real-time design preview endpoints
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class PreviewController {

    private final PreviewService previewService;

    public PreviewController(PreviewService previewService) {
        this.previewService = previewService;
    }

    /**
     * T046: Get current preview state for a session
     */
    @GetMapping("/{sessionId}/preview")
    public ResponseEntity<DesignPreviewDto> getSessionPreview(@PathVariable Long sessionId) {
        return ResponseEntity.ok(previewService.getSessionPreview(sessionId));
    }

    /**
     * T046: SSE stream for real-time preview updates
     */
    @GetMapping(value = "/{sessionId}/preview/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<org.springframework.http.codec.ServerSentEvent<DesignPreviewDto>> streamSessionPreview(
            @PathVariable Long sessionId) {
        return previewService.streamSessionPreview(sessionId)
            .map(dto -> org.springframework.http.codec.ServerSentEvent.<DesignPreviewDto>builder()
                .data(dto)
                .build());
    }
}
