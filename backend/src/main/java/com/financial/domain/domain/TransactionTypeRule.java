package com.financial.domain.domain;

import com.financial.rules.domain.AccountingRule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Join entity for the many-to-many relationship between TransactionType and AccountingRule.
 * Includes metadata like sequence number for ordering rules within a transaction type.
 */
@Entity
@Table(name = "transaction_type_rules",
       uniqueConstraints = @UniqueConstraint(name = "uk_type_rule", 
                                             columnNames = {"transaction_type_id", "rule_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTypeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_type_id", nullable = false)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private AccountingRule rule;

    @Column(name = "sequence_number", nullable = false)
    @Builder.Default
    private Integer sequenceNumber = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
