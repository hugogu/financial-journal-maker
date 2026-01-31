package com.financial.ai.service;

import com.financial.ai.domain.DesignPhase;
import com.financial.ai.domain.PromptTemplate;
import com.financial.ai.dto.PromptRequest;
import com.financial.ai.dto.PromptResponse;
import com.financial.ai.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptService {

    private final PromptRepository promptRepository;

    @Transactional(readOnly = true)
    public List<PromptResponse> getAllPrompts() {
        return promptRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromptResponse> getPromptsByPhase(DesignPhase phase) {
        return promptRepository.findByDesignPhase(phase).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromptResponse getPromptById(Long id) {
        return promptRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Prompt not found: " + id));
    }

    @Transactional(readOnly = true)
    public PromptResponse getActivePromptByPhase(DesignPhase phase) {
        return promptRepository.findByDesignPhaseAndIsActiveTrue(phase)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<PromptResponse> getPromptVersionHistory(String name) {
        return promptRepository.findByNameOrderByVersionDesc(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PromptResponse createPrompt(PromptRequest request) {
        log.info("Creating new prompt: {} for phase: {}", request.getName(), request.getDesignPhase());

        // Get the highest version for this name
        List<PromptTemplate> existingVersions = promptRepository.findByNameOrderByVersionDesc(request.getName());
        int newVersion = existingVersions.isEmpty() ? 1 : existingVersions.get(0).getVersion() + 1;

        PromptTemplate prompt = PromptTemplate.builder()
                .name(request.getName())
                .designPhase(request.getDesignPhase())
                .content(request.getContent())
                .version(newVersion)
                .isActive(false)
                .build();

        PromptTemplate saved = promptRepository.save(prompt);
        log.info("Created prompt {} with version {}", saved.getId(), saved.getVersion());

        return toResponse(saved);
    }

    @Transactional
    public PromptResponse updatePrompt(Long id, PromptRequest request) {
        log.info("Updating prompt: {}", id);

        PromptTemplate existing = promptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt not found: " + id));

        // Create a new version instead of modifying the existing one
        int newVersion = existing.getVersion() + 1;

        PromptTemplate newPrompt = PromptTemplate.builder()
                .name(request.getName())
                .designPhase(request.getDesignPhase())
                .content(request.getContent())
                .version(newVersion)
                .isActive(false)
                .build();

        PromptTemplate saved = promptRepository.save(newPrompt);
        log.info("Created new version {} for prompt {}", newVersion, saved.getName());

        return toResponse(saved);
    }

    @Transactional
    public PromptResponse activatePrompt(Long id) {
        log.info("Activating prompt: {}", id);

        PromptTemplate promptToActivate = promptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt not found: " + id));

        // Deactivate any currently active prompt for the same phase
        promptRepository.findByDesignPhaseAndIsActiveTrue(promptToActivate.getDesignPhase())
                .ifPresent(activePrompt -> {
                    activePrompt.setIsActive(false);
                    promptRepository.save(activePrompt);
                    log.info("Deactivated previous prompt: {} (version {})", 
                            activePrompt.getName(), activePrompt.getVersion());
                });

        // Activate the new prompt
        promptToActivate.setIsActive(true);
        PromptTemplate saved = promptRepository.save(promptToActivate);
        log.info("Activated prompt: {} (version {})", saved.getName(), saved.getVersion());

        return toResponse(saved);
    }

    @Transactional
    public PromptResponse rollbackToVersion(String name, Integer version) {
        log.info("Rolling back prompt {} to version {}", name, version);

        PromptTemplate targetVersion = promptRepository.findByNameAndVersion(name, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Prompt version not found: " + name + " v" + version));

        // Create a new version with the old content
        List<PromptTemplate> existingVersions = promptRepository.findByNameOrderByVersionDesc(name);
        int newVersion = existingVersions.isEmpty() ? 1 : existingVersions.get(0).getVersion() + 1;

        PromptTemplate rollbackPrompt = PromptTemplate.builder()
                .name(targetVersion.getName())
                .designPhase(targetVersion.getDesignPhase())
                .content(targetVersion.getContent())
                .version(newVersion)
                .isActive(false)
                .build();

        PromptTemplate saved = promptRepository.save(rollbackPrompt);
        log.info("Created rollback version {} from version {}", newVersion, version);

        return toResponse(saved);
    }

    @Transactional
    public void deletePrompt(Long id) {
        log.info("Deleting prompt: {}", id);

        PromptTemplate prompt = promptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt not found: " + id));

        if (prompt.getIsActive()) {
            throw new IllegalStateException("Cannot delete an active prompt. Deactivate it first.");
        }

        promptRepository.delete(prompt);
        log.info("Deleted prompt: {}", id);
    }

    @Transactional
    public void initializeDefaultPrompts() {
        log.info("Initializing default prompts");

        // Check if prompts already exist
        if (promptRepository.count() > 0) {
            log.info("Prompts already exist, skipping initialization");
            return;
        }

        createAndActivateDefaultPrompt(DesignPhase.PRODUCT, "Product Analysis",
                "You are an accounting design assistant helping to define Products. " +
                "Products represent the core offerings of a business (e.g., Loans, Deposits, Payments). " +
                "Based on the user's business description, help identify and define appropriate Products.\n\n" +
                "Current confirmed decisions: {{confirmedDecisions}}\n" +
                "Existing products in system: {{existingProducts}}\n\n" +
                "User message: {{userMessage}}");

        createAndActivateDefaultPrompt(DesignPhase.SCENARIO, "Scenario Analysis",
                "You are an accounting design assistant helping to define Scenarios. " +
                "Scenarios are specific use cases within a Product (e.g., Loan Disbursement, Interest Accrual). " +
                "Based on the confirmed Product and user's requirements, help identify appropriate Scenarios.\n\n" +
                "Current confirmed decisions: {{confirmedDecisions}}\n" +
                "Existing scenarios in system: {{existingScenarios}}\n\n" +
                "User message: {{userMessage}}");

        createAndActivateDefaultPrompt(DesignPhase.TRANSACTION_TYPE, "Transaction Type Analysis",
                "You are an accounting design assistant helping to define Transaction Types. " +
                "Transaction Types are the specific accounting events within a Scenario " +
                "(e.g., Principal Payment, Fee Collection). " +
                "Based on the confirmed Scenario, help define the necessary Transaction Types.\n\n" +
                "Current confirmed decisions: {{confirmedDecisions}}\n" +
                "Existing transaction types in system: {{existingTransactionTypes}}\n\n" +
                "User message: {{userMessage}}");

        createAndActivateDefaultPrompt(DesignPhase.ACCOUNTING, "Accounting Design",
                "You are an accounting design assistant helping to define Accounting Rules. " +
                "Based on the confirmed Transaction Types, help design the double-entry accounting rules " +
                "including debit/credit accounts, posting logic, and Numscript generation.\n\n" +
                "Current confirmed decisions: {{confirmedDecisions}}\n" +
                "Chart of Accounts: {{chartOfAccounts}}\n\n" +
                "User message: {{userMessage}}");

        log.info("Default prompts initialized");
    }

    private void createAndActivateDefaultPrompt(DesignPhase phase, String name, String content) {
        PromptTemplate prompt = PromptTemplate.builder()
                .name(name)
                .designPhase(phase)
                .content(content)
                .version(1)
                .isActive(true)
                .build();
        promptRepository.save(prompt);
        log.debug("Created default prompt: {} for phase {}", name, phase);
    }

    private PromptResponse toResponse(PromptTemplate prompt) {
        return PromptResponse.builder()
                .id(prompt.getId())
                .name(prompt.getName())
                .designPhase(prompt.getDesignPhase())
                .content(prompt.getContent())
                .version(prompt.getVersion())
                .isActive(prompt.getIsActive())
                .createdAt(prompt.getCreatedAt())
                .updatedAt(prompt.getUpdatedAt())
                .build();
    }
}
