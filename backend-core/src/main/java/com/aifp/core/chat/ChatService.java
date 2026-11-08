package com.aifp.core.chat;

import com.aifp.core.auth.UserEntity;
import com.aifp.core.auth.UserRepository;
import com.aifp.core.common.AiOrchestratorClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AiOrchestratorClient aiOrchestratorClient;

    public ChatService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            AiOrchestratorClient aiOrchestratorClient
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.aiOrchestratorClient = aiOrchestratorClient;
    }

    public record CreateConversationRequest(@NotBlank @Size(max = 200) String title) {}

    public record SendMessageRequest(@NotBlank @Size(max = 16000) String content) {}

    public record ChatTurnResponse(
            UUID conversationId,
            MessageEntity userMessage,
            MessageEntity assistantMessage,
            String selectedModel,
            boolean quotaDowngraded
    ) {}

    @Transactional
    public ConversationEntity createConversation(UUID userId, String title) {
        ConversationEntity conv = new ConversationEntity(UUID.randomUUID(), userId, title.trim());
        return conversationRepository.save(conv);
    }

    @Transactional(readOnly = true)
    public List<ConversationEntity> listConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> getConversationMessages(UUID userId, UUID conversationId) {
        // Strict Tenant Isolation: verify conversation belongs to userId first
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        return messageRepository.findByConversationIdAndUserIdOrderByCreatedAtAsc(conversationId, userId);
    }

    @Transactional
    public ChatTurnResponse sendMessage(UUID userId, UUID conversationId, @Valid SendMessageRequest request) {
        ConversationEntity conv = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getDailyTokensUsed() >= user.getDailyTokenQuota()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily AI token quota exceeded");
        }

        // Execute LangGraph Agent via HMAC internal bridge
        boolean quotaWarning = user.getDailyTokensUsed() >= (user.getDailyTokenQuota() * 0.8);
        AiOrchestratorClient.AgentExecutionResult execResult = aiOrchestratorClient.executeAgentTask(
                userId,
                request.content().trim(),
                List.of("searxng_search", "webpage_extract", "rss_fetch"),
                List.of(),
                "ALL"
        );

        String selectedModel = execResult.selectedModel() != null ? execResult.selectedModel() : "gemini-2.5-flash";
        String replyContent = execResult.finalMarkdown();

        // Build citations JSON array
        StringBuilder citationsJson = new StringBuilder("[");
        if (execResult.verifiedCitations() != null) {
            for (int i = 0; i < execResult.verifiedCitations().size(); i++) {
                if (i > 0) citationsJson.append(",");
                String url = execResult.verifiedCitations().get(i);
                String label = url.replace("https://", "").replace("http://", "").split("/")[0];
                citationsJson.append("{\"label\":\"").append(label).append("\",\"url\":\"").append(url).append("\"}");
            }
        }
        citationsJson.append("]");

        MessageEntity userMsg = new MessageEntity(
                UUID.randomUUID(),
                conversationId,
                userId,
                "USER",
                request.content().trim(),
                null,
                "[]"
        );
        messageRepository.save(userMsg);

        MessageEntity assistantMsg = new MessageEntity(
                UUID.randomUUID(),
                conversationId,
                userId,
                "ASSISTANT",
                replyContent,
                selectedModel,
                citationsJson.toString()
        );
        messageRepository.save(assistantMsg);

        user.setDailyTokensUsed(user.getDailyTokensUsed() + 350);
        userRepository.save(user);

        conv.setLastMessageAt(Instant.now());
        conversationRepository.save(conv);

        return new ChatTurnResponse(conversationId, userMsg, assistantMsg, selectedModel, quotaWarning);
    }
}

