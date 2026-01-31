package com.financial.rules.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleReferenceResponse {

    private Long ruleId;
    private String ruleCode;
    private boolean isShared;
    private int referenceCount;
    
    @Builder.Default
    private List<ScenarioReference> references = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScenarioReference {
        private String scenarioId;
        private String scenarioName;
        private String usageContext;
    }

    public boolean hasReferences() {
        return referenceCount > 0;
    }
}
