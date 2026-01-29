package com.financial.coa.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ImportJob entity tracking batch import operations for accounts.
 */
@Entity
@Table(name = "import_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ImportJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", nullable = false, length = 20)
    private FileFormat fileFormat;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ImportStatus status = ImportStatus.PENDING;
    
    @Column(name = "total_records", nullable = false)
    @Builder.Default
    private Integer totalRecords = 0;
    
    @Column(name = "processed_records", nullable = false)
    @Builder.Default
    private Integer processedRecords = 0;
    
    @Column(name = "failed_records", nullable = false)
    @Builder.Default
    private Integer failedRecords = 0;
    
    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    public enum FileFormat {
        EXCEL, CSV
    }
    
    public enum ImportStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
