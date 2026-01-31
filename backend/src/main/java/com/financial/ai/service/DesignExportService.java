package com.financial.ai.service;

import com.financial.ai.domain.*;
import com.financial.ai.dto.ExportConflictResponse;
import com.financial.ai.dto.ExportResponse;
import com.financial.ai.exception.InvalidSessionStateException;
import com.financial.ai.exception.SessionNotFoundException;
import com.financial.ai.repository.DecisionRepository;
import com.financial.ai.repository.ExportArtifactRepository;
import com.financial.ai.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DesignExportService {

    private final SessionRepository sessionRepository;
    private final DecisionRepository decisionRepository;
    private final ExportArtifactRepository exportArtifactRepository;

    @Transactional
    public ExportResponse exportDesign(Long sessionId, ExportType exportType, boolean forceOverwrite) {
        AnalysisSession session = validateSessionForExport(sessionId);
        
        List<DesignDecision> confirmedDecisions = decisionRepository
                .findBySessionIdAndIsConfirmed(sessionId, true);

        if (confirmedDecisions.isEmpty()) {
            throw new IllegalStateException("No confirmed decisions to export");
        }

        List<ExportConflictResponse> conflicts = detectConflicts(exportType, confirmedDecisions);
        
        if (!conflicts.isEmpty() && !forceOverwrite) {
            return ExportResponse.builder()
                    .sessionId(sessionId)
                    .exportType(exportType)
                    .success(false)
                    .hasConflicts(true)
                    .conflicts(conflicts)
                    .message("Export has conflicts. Use force=true to overwrite.")
                    .build();
        }

        String content = generateExportContent(exportType, confirmedDecisions);
        
        ExportArtifact artifact = ExportArtifact.builder()
                .sessionId(sessionId)
                .artifactType(exportType)
                .content(content)
                .metadata(buildExportMetadata(session, confirmedDecisions, forceOverwrite))
                .build();
        
        artifact = exportArtifactRepository.save(artifact);
        
        log.info("Exported {} for session {} (artifact id: {})", exportType, sessionId, artifact.getId());

        return ExportResponse.builder()
                .sessionId(sessionId)
                .exportType(exportType)
                .artifactId(artifact.getId())
                .content(content)
                .success(true)
                .hasConflicts(!conflicts.isEmpty())
                .conflicts(conflicts)
                .message(conflicts.isEmpty() ? "Export successful" : "Export completed with overwritten conflicts")
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExportResponse> getExportHistory(Long sessionId) {
        return exportArtifactRepository.findBySessionId(sessionId)
                .stream()
                .map(artifact -> ExportResponse.builder()
                        .sessionId(sessionId)
                        .exportType(artifact.getArtifactType())
                        .artifactId(artifact.getId())
                        .content(artifact.getContent())
                        .success(true)
                        .exportedAt(artifact.getExportedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExportConflictResponse previewConflicts(Long sessionId, ExportType exportType) {
        validateSessionForExport(sessionId);
        
        List<DesignDecision> confirmedDecisions = decisionRepository
                .findBySessionIdAndIsConfirmed(sessionId, true);

        List<ExportConflictResponse> conflicts = detectConflicts(exportType, confirmedDecisions);
        
        return ExportConflictResponse.builder()
                .exportType(exportType)
                .hasConflicts(!conflicts.isEmpty())
                .conflictCount(conflicts.size())
                .details(conflicts.stream()
                        .map(ExportConflictResponse::getDetails)
                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
                .build();
    }

    private AnalysisSession validateSessionForExport(Long sessionId) {
        AnalysisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new InvalidSessionStateException(session.getStatus(), "export");
        }

        return session;
    }

    private List<ExportConflictResponse> detectConflicts(ExportType exportType, List<DesignDecision> decisions) {
        List<ExportConflictResponse> conflicts = new ArrayList<>();
        
        for (DesignDecision decision : decisions) {
            if (decision.getLinkedEntityId() != null) {
                conflicts.add(ExportConflictResponse.builder()
                        .exportType(exportType)
                        .hasConflicts(true)
                        .conflictCount(1)
                        .details(List.of("Entity already exists: " + decision.getEntityType() + 
                                " (ID: " + decision.getLinkedEntityId() + ")"))
                        .build());
            }
        }
        
        return conflicts;
    }

    private String generateExportContent(ExportType exportType, List<DesignDecision> decisions) {
        return switch (exportType) {
            case COA -> generateCoaExport(decisions);
            case RULES -> generateRulesExport(decisions);
            case NUMSCRIPT -> generateNumscriptExport(decisions);
        };
    }

    private String generateCoaExport(List<DesignDecision> decisions) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Chart of Accounts Export\n");
        sb.append("# Generated from AI Analysis Session\n\n");

        for (DesignDecision decision : decisions) {
            if (decision.getDecisionType() == DesignPhase.ACCOUNTING) {
                Map<String, Object> content = decision.getContent();
                sb.append("## Account: ").append(content.getOrDefault("name", "Unnamed")).append("\n");
                sb.append("Type: ").append(content.getOrDefault("type", "ASSET")).append("\n");
                sb.append("Code: ").append(content.getOrDefault("code", "")).append("\n");
                sb.append("Description: ").append(content.getOrDefault("description", "")).append("\n\n");
            }
        }

        return sb.toString();
    }

    private String generateRulesExport(List<DesignDecision> decisions) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Accounting Rules Export\n");
        sb.append("# Generated from AI Analysis Session\n\n");

        for (DesignDecision decision : decisions) {
            Map<String, Object> content = decision.getContent();
            sb.append("## Rule: ").append(decision.getDecisionType()).append("\n");
            sb.append("Entity: ").append(decision.getEntityType()).append("\n");
            sb.append("Configuration:\n");
            content.forEach((key, value) -> 
                sb.append("  ").append(key).append(": ").append(value).append("\n"));
            sb.append("\n");
        }

        return sb.toString();
    }

    private String generateNumscriptExport(List<DesignDecision> decisions) {
        StringBuilder sb = new StringBuilder();
        sb.append("// Numscript Export\n");
        sb.append("// Generated from AI Analysis Session\n\n");

        List<DesignDecision> accountingDecisions = decisions.stream()
                .filter(d -> d.getDecisionType() == DesignPhase.ACCOUNTING)
                .collect(Collectors.toList());

        if (accountingDecisions.isEmpty()) {
            sb.append("// No accounting decisions to export\n");
            return sb.toString();
        }

        sb.append("vars {\n");
        for (DesignDecision decision : accountingDecisions) {
            Map<String, Object> content = decision.getContent();
            String name = (String) content.getOrDefault("name", "account");
            sb.append("  account $").append(name.toLowerCase().replace(" ", "_")).append("\n");
        }
        sb.append("}\n\n");

        sb.append("send [\n");
        sb.append("  // Define transaction amounts here\n");
        sb.append("] (\n");
        sb.append("  source = @world\n");
        sb.append("  destination = // Target account\n");
        sb.append(")\n");

        return sb.toString();
    }

    private Map<String, Object> buildExportMetadata(AnalysisSession session, 
                                                      List<DesignDecision> decisions,
                                                      boolean forceOverwrite) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sessionTitle", session.getTitle());
        metadata.put("decisionCount", decisions.size());
        metadata.put("forceOverwrite", forceOverwrite);
        metadata.put("phases", decisions.stream()
                .map(d -> d.getDecisionType().name())
                .distinct()
                .collect(Collectors.toList()));
        return metadata;
    }
}
