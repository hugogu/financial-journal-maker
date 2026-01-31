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
public class VersionResponse {

    private Long id;
    private Long ruleId;
    private Integer versionNumber;
    private String snapshotJson;
    private String changeDescription;
    private LocalDateTime createdAt;
    private String createdBy;

    public static VersionResponse fromEntity(AccountingRuleVersion version) {
        return VersionResponse.builder()
                .id(version.getId())
                .ruleId(version.getRuleId())
                .versionNumber(version.getVersionNumber())
                .snapshotJson(version.getSnapshotJson())
                .changeDescription(version.getChangeDescription())
                .createdAt(version.getCreatedAt())
                .createdBy(version.getCreatedBy())
                .build();
    }
}
