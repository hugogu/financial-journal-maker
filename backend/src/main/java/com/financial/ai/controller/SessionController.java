package com.financial.ai.controller;

import com.financial.ai.domain.ExportType;
import com.financial.ai.domain.SessionStatus;
import com.financial.ai.dto.*;
import com.financial.ai.service.AIConversationService;
import com.financial.ai.service.DecisionService;
import com.financial.ai.service.DesignExportService;
import com.financial.ai.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Analysis session management")
public class SessionController {

    private final SessionService sessionService;
    private final AIConversationService conversationService;
    private final DecisionService decisionService;
    private final DesignExportService exportService;

    @GetMapping
    @Operation(summary = "List sessions", description = "Get paginated list of analysis sessions")
    public ResponseEntity<Page<SessionResponse>> listSessions(
            @Parameter(description = "Filter by status") @RequestParam(required = false) SessionStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(sessionService.listSessions(status, pageable));
    }

    @PostMapping
    @Operation(summary = "Create session", description = "Create a new analysis session")
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody SessionCreateRequest request) {
        SessionResponse response = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session", description = "Get session details including current design state")
    public ResponseEntity<SessionDetailResponse> getSession(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getSessionDetail(sessionId));
    }

    @PatchMapping("/{sessionId}")
    @Operation(summary = "Update session", description = "Update session title or other metadata")
    public ResponseEntity<SessionResponse> updateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionUpdateRequest request) {
        return ResponseEntity.ok(sessionService.updateSession(sessionId, request));
    }

    @PostMapping("/{sessionId}/pause")
    @Operation(summary = "Pause session", description = "Pause an active session to resume later")
    public ResponseEntity<SessionResponse> pauseSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.pauseSession(sessionId));
    }

    @PostMapping("/{sessionId}/resume")
    @Operation(summary = "Resume session", description = "Resume a paused session")
    public ResponseEntity<SessionResponse> resumeSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.resumeSession(sessionId));
    }

    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Complete session", description = "Mark session as completed (becomes read-only)")
    public ResponseEntity<SessionResponse> completeSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.completeSession(sessionId));
    }

    @PostMapping("/{sessionId}/archive")
    @Operation(summary = "Archive session", description = "Archive a completed session")
    public ResponseEntity<SessionResponse> archiveSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.archiveSession(sessionId));
    }

    @GetMapping("/{sessionId}/messages")
    @Operation(summary = "Get messages", description = "Get conversation history for a session")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(conversationService.getMessages(sessionId));
    }

    @PostMapping("/{sessionId}/messages")
    @Operation(summary = "Send message", description = "Send a message and get AI response (non-streaming)")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(conversationService.sendMessage(sessionId, request));
    }

    @PostMapping(value = "/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Send message (streaming)", description = "Send a message and receive AI response as SSE stream")
    public Flux<String> sendMessageStream(
            @PathVariable Long sessionId,
            @Valid @RequestBody MessageRequest request) {
        return conversationService.streamMessage(sessionId, request)
                .map(chunk -> "data: " + chunk.replace("\n", "\\n") + "\n\n");
    }

    @GetMapping("/{sessionId}/decisions")
    @Operation(summary = "Get decisions", description = "Get all design decisions for a session")
    public ResponseEntity<List<DecisionResponse>> getDecisions(
            @PathVariable Long sessionId,
            @Parameter(description = "Filter by confirmation status") @RequestParam(required = false) Boolean confirmed) {
        return ResponseEntity.ok(decisionService.getDecisions(sessionId, confirmed));
    }

    @PostMapping("/{sessionId}/decisions")
    @Operation(summary = "Confirm decision", description = "Confirm or update a design decision")
    public ResponseEntity<DecisionResponse> confirmDecision(
            @PathVariable Long sessionId,
            @Valid @RequestBody DecisionRequest request) {
        return ResponseEntity.ok(decisionService.createOrUpdateDecision(sessionId, request));
    }

    @PostMapping("/{sessionId}/export/{type}")
    @Operation(summary = "Export design", description = "Export session design as COA, rules, or Numscript")
    public ResponseEntity<ExportResponse> exportDesign(
            @PathVariable Long sessionId,
            @PathVariable ExportType type,
            @Parameter(description = "Force overwrite existing data") @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(exportService.exportDesign(sessionId, type, force));
    }

    @GetMapping("/{sessionId}/export")
    @Operation(summary = "Get export history", description = "Get all export artifacts for a session")
    public ResponseEntity<List<ExportResponse>> getExportHistory(@PathVariable Long sessionId) {
        return ResponseEntity.ok(exportService.getExportHistory(sessionId));
    }

    @GetMapping("/{sessionId}/export/{type}/conflicts")
    @Operation(summary = "Preview conflicts", description = "Preview potential export conflicts without executing")
    public ResponseEntity<ExportConflictResponse> previewConflicts(
            @PathVariable Long sessionId,
            @PathVariable ExportType type) {
        return ResponseEntity.ok(exportService.previewConflicts(sessionId, type));
    }
}
