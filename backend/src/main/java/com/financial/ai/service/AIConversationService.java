package com.financial.ai.service;

import com.financial.ai.domain.*;
import com.financial.ai.dto.DecisionResponse;
import com.financial.ai.dto.MessageRequest;
import com.financial.ai.dto.MessageResponse;
import com.financial.ai.exception.InvalidSessionStateException;
import com.financial.ai.exception.SessionNotFoundException;
import com.financial.ai.repository.MessageRepository;
import com.financial.ai.repository.PromptRepository;
import com.financial.ai.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIConversationService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final PromptRepository promptRepository;
    private final LLMClientProvider llmClientProvider;
    private final DecisionService decisionService;

    @Transactional
    public MessageResponse sendMessage(Long sessionId, MessageRequest request) {
        AnalysisSession session = validateAndGetSession(sessionId);

        SessionMessage userMessage = saveUserMessage(sessionId, request.getContent());

        String systemPrompt = buildSystemPrompt(session);
        String conversationContext = buildConversationContext(sessionId);

        ChatClient chatClient = llmClientProvider.getChatClientOrThrow();
        
        String aiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(conversationContext + "\n\nUser: " + request.getContent())
                .call()
                .content();

        SessionMessage assistantMessage = saveAssistantMessage(sessionId, aiResponse);

        log.info("Processed message for session {} in phase {}", sessionId, session.getCurrentPhase());

        return toMessageResponse(assistantMessage);
    }

    public Flux<String> streamMessage(Long sessionId, MessageRequest request) {
        AnalysisSession session = validateAndGetSession(sessionId);

        saveUserMessage(sessionId, request.getContent());

        String systemPrompt = buildSystemPrompt(session);
        String conversationContext = buildConversationContext(sessionId);

        log.debug("STREAMING - Getting ChatClient for session {}", sessionId);
        ChatClient chatClient = llmClientProvider.getChatClientOrThrow();
        log.debug("STREAMING - ChatClient obtained, starting stream");

        StringBuilder fullResponse = new StringBuilder();

        Flux<String> contentStream = chatClient.prompt()
                .system(systemPrompt)
                .user(conversationContext + "\n\nUser: " + request.getContent())
                .stream()
                .content();
        
        log.debug("STREAMING - Content stream created, subscribing to chunks");
        
        return contentStream
                .doOnSubscribe(sub -> log.debug("STREAMING - Stream subscribed"))
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                    log.debug("STREAMING - Emitting chunk of length: {}", chunk.length());
                })
                .doOnComplete(() -> {
                    log.info("STREAMING - Stream completed, saving full response of length: {}", fullResponse.length());
                    saveAssistantMessage(sessionId, fullResponse.toString());
                })
                .doOnError(error -> {
                    log.error("STREAMING - Error streaming response for session {}: {}", sessionId, error.getMessage(), error);
                });
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    public DesignPhase suggestNextPhase(Long sessionId) {
        AnalysisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        List<DecisionResponse> confirmedDecisions = decisionService.getConfirmedDecisions(sessionId);
        DesignPhase currentPhase = session.getCurrentPhase();

        boolean hasConfirmedAtCurrentPhase = confirmedDecisions.stream()
                .anyMatch(d -> d.getDecisionType() == currentPhase && d.getIsConfirmed());

        if (hasConfirmedAtCurrentPhase) {
            return getNextPhase(currentPhase);
        }

        return currentPhase;
    }

    private DesignPhase getNextPhase(DesignPhase currentPhase) {
        return switch (currentPhase) {
            case PRODUCT -> DesignPhase.SCENARIO;
            case SCENARIO -> DesignPhase.TRANSACTION_TYPE;
            case TRANSACTION_TYPE -> DesignPhase.ACCOUNTING;
            case ACCOUNTING, SYSTEM -> DesignPhase.ACCOUNTING;
        };
    }

    private AnalysisSession validateAndGetSession(Long sessionId) {
        AnalysisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new InvalidSessionStateException(session.getStatus(), "send message");
        }

        return session;
    }

    private SessionMessage saveUserMessage(Long sessionId, String content) {
        SessionMessage message = SessionMessage.builder()
                .sessionId(sessionId)
                .role(MessageRole.USER)
                .content(content)
                .build();
        return messageRepository.save(message);
    }

    private SessionMessage saveAssistantMessage(Long sessionId, String content) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("model", "configured");
        
        SessionMessage message = SessionMessage.builder()
                .sessionId(sessionId)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .metadata(metadata)
                .build();
        return messageRepository.save(message);
    }

    private String buildSystemPrompt(AnalysisSession session) {
        DesignPhase phase = session.getCurrentPhase();
        
        return promptRepository.findByDesignPhaseAndIsActiveTrue(phase)
                .map(PromptTemplate::getContent)
                .orElseGet(() -> getDefaultSystemPrompt(phase));
    }

    private String getDefaultSystemPrompt(DesignPhase phase) {
        String basePrompt = """
            You are an accounting design assistant helping users design financial processes.
            You are currently in the %s phase of the design process.
            
            The design hierarchy is: Product → Scenario → TransactionType → Accounting
            
            Guidelines:
            - Provide clear, structured suggestions
            - Maintain consistency with previously confirmed decisions
            - Explain the reasoning behind your suggestions
            - Ask clarifying questions when needed
            - Suggest next steps when appropriate
            """;

        return String.format(basePrompt, phase.name());
    }

    private String buildConversationContext(Long sessionId) {
        List<SessionMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        
        if (messages.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("Previous conversation:\n");
        int startIndex = Math.max(0, messages.size() - 10);
        
        for (int i = startIndex; i < messages.size(); i++) {
            SessionMessage msg = messages.get(i);
            String role = msg.getRole() == MessageRole.USER ? "User" : "Assistant";
            context.append(role).append(": ").append(msg.getContent()).append("\n\n");
        }

        return context.toString();
    }

    private MessageResponse toMessageResponse(SessionMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .metadata(message.getMetadata())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
