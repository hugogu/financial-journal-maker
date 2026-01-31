package com.financial.rules.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounting_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RuleStatus status = RuleStatus.DRAFT;

    @Column(name = "shared_across_scenarios", nullable = false)
    @Builder.Default
    private Boolean sharedAcrossScenarios = false;

    @Column(name = "current_version", nullable = false)
    @Builder.Default
    private Integer currentVersion = 1;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToOne(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EntryTemplate entryTemplate;

    public boolean isDraft() {
        return status == RuleStatus.DRAFT;
    }

    public boolean isActive() {
        return status == RuleStatus.ACTIVE;
    }

    public boolean isArchived() {
        return status == RuleStatus.ARCHIVED;
    }

    public boolean canActivate() {
        return status == RuleStatus.DRAFT;
    }

    public boolean canArchive() {
        return status == RuleStatus.DRAFT || status == RuleStatus.ACTIVE;
    }

    public boolean canRestore() {
        return status == RuleStatus.ARCHIVED;
    }

    public boolean canUpdate() {
        return status == RuleStatus.DRAFT;
    }

    public boolean canDelete() {
        return status != RuleStatus.ACTIVE;
    }
}
