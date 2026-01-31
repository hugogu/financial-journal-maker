package com.financial.ai.dto;

import com.financial.ai.domain.MessageRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private MessageRole role;
    private String content;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
