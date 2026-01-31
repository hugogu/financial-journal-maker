package com.financial.ai.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionUpdateRequest {

    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;
}
