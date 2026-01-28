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
 * AccountReference entity tracking where accounts are referenced (by rules or scenarios).
 * Used to enforce immutability constraints on referenced accounts.
 */
@Entity
@Table(name = "account_references",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_account_ref_composite",
           columnNames = {"account_code", "reference_source_id", "reference_type"}
       ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AccountReference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;
    
    @Column(name = "reference_source_id", nullable = false, length = 255)
    private String referenceSourceId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 50)
    private ReferenceType referenceType;
    
    @Column(name = "reference_description", length = 500)
    private String referenceDescription;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Type of reference: RULE or SCENARIO
     */
    public enum ReferenceType {
        RULE,
        SCENARIO
    }
}
