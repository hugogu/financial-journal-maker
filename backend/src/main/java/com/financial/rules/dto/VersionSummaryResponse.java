package com.financial.rules.dto;

import com.financial.rules.domain.AccountingRuleVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionSummaryResponse {

    private Long id;
    private Integer versionNumber;
    private String changeDescription;
    private LocalDateTime createdAt;
    private String createdBy;

    public static VersionSummaryResponse fromEntity(AccountingRuleVersion version) {
        return VersionSummaryResponse.builder()
                .id(version.getId())
                .versionNumber(version.getVersionNumber())
                .changeDescription(version.getChangeDescription())
                .createdAt(version.getCreatedAt())
                .createdBy(version.getCreatedBy())
                .build();
    }
}
