package com.financial.ai.dto;

import com.financial.ai.domain.ExportType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportResponse {

    private Long sessionId;
    private ExportType exportType;
    private Long artifactId;
    private String content;
    private boolean success;
    private boolean hasConflicts;
    private List<ExportConflictResponse> conflicts;
    private String message;
    private LocalDateTime exportedAt;
}
