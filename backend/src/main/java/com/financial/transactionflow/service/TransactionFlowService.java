package com.financial.transactionflow.service;

import com.financial.ai.domain.DesignDecision;
import com.financial.ai.domain.DesignPhase;
import com.financial.ai.domain.ExportArtifact;
import com.financial.ai.domain.ExportType;
import com.financial.ai.repository.DecisionRepository;
import com.financial.ai.repository.ExportArtifactRepository;
import com.financial.transactionflow.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for aggregating and transforming transaction flow data from DesignDecisions.
 * This is a read-only service that queries existing AI session data.
 */
@Service
public class TransactionFlowService {

    private final DecisionRepository decisionRepository;
    private final ExportArtifactRepository artifactRepository;

    public TransactionFlowService(DecisionRepository decisionRepository, 
                                   ExportArtifactRepository artifactRepository) {
        this.decisionRepository = decisionRepository;
        this.artifactRepository = artifactRepository;
    }

    /**
     * T017: List all products with transaction flow designs
     */
    public Page<ProductSummary> listProducts(String search, Pageable pageable) {
        List<DesignDecision> productDecisions = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.PRODUCT && d.getIsConfirmed())
            .collect(Collectors.toList());

        List<ProductSummary> products = productDecisions.stream()
            .map(this::mapToProductSummary)
            .filter(p -> search == null || search.isEmpty() || 
                   p.getProductName().toLowerCase().contains(search.toLowerCase()) ||
                   p.getProductCode().toLowerCase().contains(search.toLowerCase()))
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        
        return new PageImpl<>(
            products.subList(start, end),
            pageable,
            products.size()
        );
    }

    /**
     * T018: Get product details with scenarios
     */
    public ProductSummary getProduct(String productCode) {
        DesignDecision productDecision = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.PRODUCT && 
                        d.getIsConfirmed() &&
                        productCode.equals(d.getContent().get("code")))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Product not found: " + productCode));

        return mapToProductSummary(productDecision);
    }

    /**
     * T018: List scenarios for a product
     */
    public List<ScenarioSummary> listScenarios(String productCode) {
        return decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.SCENARIO && 
                        d.getIsConfirmed() &&
                        productCode.equals(d.getContent().get("productCode")))
            .map(this::mapToScenarioSummary)
            .collect(Collectors.toList());
    }

    /**
     * T019: List all transaction flows with filters
     */
    public Page<TransactionFlowSummary> listAllTransactionFlows(
            String productCode, String scenarioCode, String search, Pageable pageable) {
        
        List<DesignDecision> transactionDecisions = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.TRANSACTION_TYPE && d.getIsConfirmed())
            .filter(d -> productCode == null || productCode.isEmpty() || 
                        matchesProduct(d, productCode))
            .filter(d -> scenarioCode == null || scenarioCode.isEmpty() || 
                        scenarioCode.equals(d.getContent().get("scenarioCode")))
            .filter(d -> search == null || search.isEmpty() || 
                        matchesSearch(d, search))
            .collect(Collectors.toList());

        List<TransactionFlowSummary> summaries = transactionDecisions.stream()
            .map(this::mapToTransactionFlowSummary)
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), summaries.size());
        
        return new PageImpl<>(
            summaries.subList(start, end),
            pageable,
            summaries.size()
        );
    }

    /**
     * T028: Get complete transaction flow details
     */
    public TransactionFlowView getTransactionFlow(String transactionTypeCode) {
        DesignDecision transactionDecision = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.TRANSACTION_TYPE && 
                        d.getIsConfirmed() &&
                        transactionTypeCode.equals(d.getContent().get("code")))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Transaction flow not found: " + transactionTypeCode));

        // Find associated accounting decision
        DesignDecision accountingDecision = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.ACCOUNTING && 
                        d.getIsConfirmed() &&
                        transactionTypeCode.equals(d.getContent().get("transactionTypeCode")))
            .findFirst()
            .orElse(null);

        return mapToTransactionFlowView(transactionDecision, accountingDecision);
    }

    // Helper methods
    private ProductSummary mapToProductSummary(DesignDecision decision) {
        ProductSummary summary = new ProductSummary();
        Map<String, Object> content = decision.getContent();
        
        summary.setProductCode((String) content.get("code"));
        summary.setProductName((String) content.get("name"));
        summary.setDescription((String) content.get("description"));
        summary.setSourceSessionId(decision.getSessionId());
        summary.setCreatedAt(decision.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        
        // Count scenarios and transactions
        String productCode = (String) content.get("code");
        long scenarioCount = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.SCENARIO && 
                        d.getIsConfirmed() &&
                        productCode.equals(d.getContent().get("productCode")))
            .count();
        
        long transactionCount = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.TRANSACTION_TYPE && 
                        d.getIsConfirmed() &&
                        matchesProduct(d, productCode))
            .count();
        
        summary.setScenarioCount((int) scenarioCount);
        summary.setTransactionTypeCount((int) transactionCount);
        
        return summary;
    }

    private ScenarioSummary mapToScenarioSummary(DesignDecision decision) {
        ScenarioSummary summary = new ScenarioSummary();
        Map<String, Object> content = decision.getContent();
        
        summary.setScenarioCode((String) content.get("code"));
        summary.setScenarioName((String) content.get("name"));
        summary.setDescription((String) content.get("description"));
        summary.setProductCode((String) content.get("productCode"));
        summary.setSourceSessionId(decision.getSessionId());
        summary.setCreatedAt(decision.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        
        // Count transactions
        String scenarioCode = (String) content.get("code");
        long transactionCount = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.TRANSACTION_TYPE && 
                        d.getIsConfirmed() &&
                        scenarioCode.equals(d.getContent().get("scenarioCode")))
            .count();
        
        summary.setTransactionTypeCount((int) transactionCount);
        
        return summary;
    }

    private TransactionFlowSummary mapToTransactionFlowSummary(DesignDecision decision) {
        TransactionFlowSummary summary = new TransactionFlowSummary();
        Map<String, Object> content = decision.getContent();
        
        summary.setTransactionTypeCode((String) content.get("code"));
        summary.setTransactionTypeName((String) content.get("name"));
        summary.setDescription((String) content.get("description"));
        summary.setScenarioCode((String) content.get("scenarioCode"));
        summary.setSourceSessionId(decision.getSessionId());
        
        // Find product code through scenario
        String scenarioCode = (String) content.get("scenarioCode");
        decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.SCENARIO && 
                        scenarioCode.equals(d.getContent().get("code")))
            .findFirst()
            .ifPresent(scenario -> 
                summary.setProductCode((String) scenario.getContent().get("productCode")));
        
        // Check for accounting data
        String transactionCode = (String) content.get("code");
        decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.ACCOUNTING && 
                        d.getIsConfirmed() &&
                        transactionCode.equals(d.getContent().get("transactionTypeCode")))
            .findFirst()
            .ifPresent(accounting -> {
                Map<String, Object> accContent = accounting.getContent();
                List<?> accounts = (List<?>) accContent.get("accounts");
                List<?> entries = (List<?>) accContent.get("entries");
                summary.setAccountCount(accounts != null ? accounts.size() : 0);
                summary.setEntryCount(entries != null ? entries.size() : 0);
                summary.setHasNumscript(true);
            });
        
        return summary;
    }

    @SuppressWarnings("unchecked")
    private TransactionFlowView mapToTransactionFlowView(
            DesignDecision transactionDecision, DesignDecision accountingDecision) {
        
        TransactionFlowView view = new TransactionFlowView();
        Map<String, Object> content = transactionDecision.getContent();
        
        view.setTransactionTypeCode((String) content.get("code"));
        view.setTransactionTypeName((String) content.get("name"));
        view.setDescription((String) content.get("description"));
        view.setScenarioCode((String) content.get("scenarioCode"));
        view.setSourceSessionId(transactionDecision.getSessionId());
        view.setCreatedAt(transactionDecision.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        view.setUpdatedAt(transactionDecision.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant());
        
        // Find product code
        String scenarioCode = (String) content.get("scenarioCode");
        decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.SCENARIO && 
                        scenarioCode.equals(d.getContent().get("code")))
            .findFirst()
            .ifPresent(scenario -> 
                view.setProductCode((String) scenario.getContent().get("productCode")));
        
        // Map accounting data if available
        if (accountingDecision != null) {
            Map<String, Object> accContent = accountingDecision.getContent();
            
            // Map accounts
            List<Map<String, Object>> accounts = (List<Map<String, Object>>) accContent.get("accounts");
            if (accounts != null) {
                view.setAccounts(accounts.stream()
                    .map(this::mapToAccountNode)
                    .collect(Collectors.toList()));
            } else {
                view.setAccounts(new ArrayList<>());
            }
            
            // Map journal entries
            List<Map<String, Object>> entries = (List<Map<String, Object>>) accContent.get("entries");
            if (entries != null) {
                view.setJournalEntries(entries.stream()
                    .map(this::mapToJournalEntry)
                    .collect(Collectors.toList()));
            } else {
                view.setJournalEntries(new ArrayList<>());
            }
            
            // Map flow connections
            List<Map<String, Object>> flows = (List<Map<String, Object>>) accContent.get("flows");
            if (flows != null) {
                view.setFlowConnections(flows.stream()
                    .map(this::mapToFlowConnection)
                    .collect(Collectors.toList()));
            } else {
                view.setFlowConnections(new ArrayList<>());
            }
            
            view.setNumscriptValid(true);
        } else {
            view.setAccounts(new ArrayList<>());
            view.setJournalEntries(new ArrayList<>());
            view.setFlowConnections(new ArrayList<>());
            view.setNumscriptValid(false);
        }
        
        return view;
    }

    private AccountNodeDto mapToAccountNode(Map<String, Object> accountData) {
        AccountNodeDto node = new AccountNodeDto();
        node.setAccountCode((String) accountData.get("code"));
        node.setAccountName((String) accountData.get("name"));
        
        String typeStr = (String) accountData.get("type");
        if (typeStr != null) {
            node.setAccountType(AccountType.valueOf(typeStr));
        }
        
        String stateStr = (String) accountData.get("state");
        if (stateStr != null) {
            node.setAccountState(AccountState.valueOf(stateStr));
        }
        
        node.setLinkedToCoA(true); // Assume linked for now
        
        return node;
    }

    private JournalEntryDisplayDto mapToJournalEntry(Map<String, Object> entryData) {
        JournalEntryDisplayDto entry = new JournalEntryDisplayDto();
        entry.setEntryId(UUID.randomUUID().toString());
        entry.setOperation((String) entryData.get("operation"));
        entry.setAccountCode((String) entryData.get("accountCode"));
        entry.setAmountExpression((String) entryData.get("amountExpression"));
        entry.setTriggerEvent((String) entryData.get("triggerEvent"));
        entry.setCondition((String) entryData.get("condition"));
        
        // Find account name
        String accountCode = (String) entryData.get("accountCode");
        entry.setAccountName(accountCode); // Default to code
        
        return entry;
    }

    private FlowConnectionDto mapToFlowConnection(Map<String, Object> flowData) {
        FlowConnectionDto connection = new FlowConnectionDto();
        connection.setConnectionId(UUID.randomUUID().toString());
        connection.setSourceAccountCode((String) flowData.get("source"));
        connection.setTargetAccountCode((String) flowData.get("target"));
        connection.setAmountExpression((String) flowData.get("amount"));
        
        String typeStr = (String) flowData.get("type");
        if (typeStr != null) {
            connection.setFlowType(FlowType.valueOf(typeStr));
        }
        
        return connection;
    }

    /**
     * T037: Get Numscript code for a transaction type
     */
    public NumscriptViewDto getNumscript(String transactionTypeCode) {
        // Try to find from ExportArtifact first
        Optional<ExportArtifact> artifact = artifactRepository.findAll().stream()
            .filter(a -> ExportType.NUMSCRIPT.equals(a.getArtifactType()) &&
                        a.getMetadata() != null &&
                        a.getMetadata().containsKey("transactionTypeCode") &&
                        transactionTypeCode.equals(a.getMetadata().get("transactionTypeCode")))
            .findFirst();

        if (artifact.isPresent()) {
            ExportArtifact art = artifact.get();
            String code = art.getContent() != null ? art.getContent() : "";
            return NumscriptViewDto.builder()
                .transactionTypeCode(transactionTypeCode)
                .transactionTypeName((String) art.getMetadata().get("transactionTypeName"))
                .numscriptCode(code)
                .numscriptValid(true)
                .source("EXPORT_ARTIFACT")
                .lastUpdated(art.getExportedAt().toString())
                .lineCount(code.split("\n").length)
                .build();
        }

        // Generate from accounting data if no artifact exists
        DesignDecision transactionDecision = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.TRANSACTION_TYPE && 
                        d.getIsConfirmed() &&
                        transactionTypeCode.equals(d.getContent().get("code")))
            .findFirst()
            .orElse(null);

        DesignDecision accountingDecision = decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.ACCOUNTING && 
                        d.getIsConfirmed() &&
                        transactionTypeCode.equals(d.getContent().get("transactionTypeCode")))
            .findFirst()
            .orElse(null);

        if (transactionDecision == null) {
            throw new RuntimeException("Transaction type not found: " + transactionTypeCode);
        }

        String generatedCode = generateNumscript(transactionDecision, accountingDecision);
        
        return NumscriptViewDto.builder()
            .transactionTypeCode(transactionTypeCode)
            .transactionTypeName((String) transactionDecision.getContent().get("name"))
            .numscriptCode(generatedCode)
            .numscriptValid(accountingDecision != null)
            .validationError(accountingDecision == null ? "No accounting configuration found" : null)
            .source("GENERATED")
            .lastUpdated(transactionDecision.getUpdatedAt().toString())
            .lineCount(generatedCode.split("\n").length)
            .build();
    }

    @SuppressWarnings("unchecked")
    private String generateNumscript(DesignDecision transactionDecision, DesignDecision accountingDecision) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated Numscript for: ")
          .append(transactionDecision.getContent().get("name"))
          .append("\n\n");

        if (accountingDecision == null) {
            sb.append("# No accounting configuration available\n");
            return sb.toString();
        }

        Map<String, Object> accContent = accountingDecision.getContent();
        List<Map<String, Object>> accounts = (List<Map<String, Object>>) accContent.get("accounts");
        List<Map<String, Object>> entries = (List<Map<String, Object>>) accContent.get("entries");

        if (accounts != null && !accounts.isEmpty()) {
            sb.append("# Accounts\n");
            for (Map<String, Object> account : accounts) {
                sb.append("account ").append(account.get("code"))
                  .append(" \"").append(account.get("name")).append("\"\n");
            }
            sb.append("\n");
        }

        if (entries != null && !entries.isEmpty()) {
            sb.append("# Journal Entries\n");
            for (Map<String, Object> entry : entries) {
                String trigger = (String) entry.get("triggerEvent");
                String op = (String) entry.get("operation");
                String accCode = (String) entry.get("accountCode");
                String amount = (String) entry.get("amountExpression");
                
                sb.append("when ").append(trigger != null ? trigger : "*").append(":\n");
                sb.append("  ").append(op.toLowerCase())
                  .append(" ").append(accCode)
                  .append(" ").append(amount != null ? amount : "0")
                  .append("\n");
            }
        }

        return sb.toString();
    }

    private boolean matchesProduct(DesignDecision decision, String productCode) {
        String scenarioCode = (String) decision.getContent().get("scenarioCode");
        if (scenarioCode == null) return false;
        
        return decisionRepository.findAll().stream()
            .filter(d -> d.getDecisionType() == DesignPhase.SCENARIO && 
                        scenarioCode.equals(d.getContent().get("code")))
            .anyMatch(scenario -> productCode.equals(scenario.getContent().get("productCode")));
    }

    private boolean matchesSearch(DesignDecision decision, String search) {
        String name = (String) decision.getContent().get("name");
        String code = (String) decision.getContent().get("code");
        String searchLower = search.toLowerCase();
        
        return (name != null && name.toLowerCase().contains(searchLower)) ||
               (code != null && code.toLowerCase().contains(searchLower));
    }
}
