package com.financial.coa.dto;

import com.financial.coa.domain.ImportJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for import job status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobResponse {
    
    private Long id;
    private String fileName;
    private ImportJob.FileFormat fileFormat;
    private ImportJob.ImportStatus status;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer failedRecords;
    private String errorDetails;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
