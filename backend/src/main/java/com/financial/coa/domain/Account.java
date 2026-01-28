package com.financial.coa.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Account entity representing a single account in the chart of accounts hierarchy.
 * Supports self-referential parent-child relationships for tree structure.
 */
@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_account_parent"))
    private Account parent;
    
    @Column(name = "shared_across_scenarios", nullable = false)
    private Boolean sharedAcrossScenarios = false;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    /**
     * Transient field to track if this account has child accounts.
     * Populated by service layer when needed.
     */
    @Transient
    private Boolean hasChildren;
    
    /**
     * Transient field to track if this account is referenced by rules/scenarios.
     * Populated by service layer when needed.
     */
    @Transient
    private Boolean isReferenced;
    
    /**
     * Transient field to count how many references exist.
     * Populated by service layer when needed.
     */
    @Transient
    private Integer referenceCount;
}
