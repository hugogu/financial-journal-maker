package com.financial.rules.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entry_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false, unique = true)
    private AccountingRule rule;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "variable_schema_json", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String variableSchemaJson = "[]";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceNumber ASC")
    @Builder.Default
    private List<EntryLine> lines = new ArrayList<>();

    public void addLine(EntryLine line) {
        lines.add(line);
        line.setTemplate(this);
    }

    public void removeLine(EntryLine line) {
        lines.remove(line);
        line.setTemplate(null);
    }

    public void clearLines() {
        lines.forEach(line -> line.setTemplate(null));
        lines.clear();
    }
}
