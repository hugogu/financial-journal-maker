package com.financial.ai.dto;

import com.financial.ai.domain.ExportType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportConflictResponse {

    private ExportType exportType;
    private boolean hasConflicts;
    private int conflictCount;
    private List<String> details;
}
