package com.financial.coa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a node in the account tree hierarchy.
 * Supports recursive structure for nested accounts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountTreeNode {
    
    private String code;
    private String name;
    private String description;
    private Boolean isReferenced;
    
    @Builder.Default
    private List<AccountTreeNode> children = new ArrayList<>();
}
