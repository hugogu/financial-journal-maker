package com.financial.domain.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Product entity representing a business product line.
 * Root node of the hierarchy: Product → Scenario → TransactionType.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_model", columnDefinition = "TEXT")
    private String businessModel;

    @Column(columnDefinition = "TEXT")
    private String participants;

    @Column(name = "fund_flow", columnDefinition = "TEXT")
    private String fundFlow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EntityStatus status = EntityStatus.DRAFT;

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

    public boolean isDraft() {
        return status == EntityStatus.DRAFT;
    }

    public boolean isActive() {
        return status == EntityStatus.ACTIVE;
    }

    public boolean isArchived() {
        return status == EntityStatus.ARCHIVED;
    }

    public boolean canActivate() {
        return status == EntityStatus.DRAFT;
    }

    public boolean canArchive() {
        return status == EntityStatus.DRAFT || status == EntityStatus.ACTIVE;
    }

    public boolean canRestore() {
        return status == EntityStatus.ARCHIVED;
    }

    public boolean canUpdate() {
        return status == EntityStatus.DRAFT;
    }

    public boolean canDelete() {
        return status != EntityStatus.ACTIVE;
    }
}
