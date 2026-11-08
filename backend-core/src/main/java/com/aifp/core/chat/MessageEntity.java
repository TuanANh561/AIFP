package com.aifp.core.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_messages")
public class MessageEntity {

    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "prompt_tokens")
    private int promptTokens = 0;

    @Column(name = "completion_tokens")
    private int completionTokens = 0;

    @Column(name = "citations_json", nullable = false)
    private String citationsJson = "[]";

    @Column(name = "tool_calls_json", nullable = false)
    private String toolCallsJson = "[]";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public MessageEntity() {
    }

    public MessageEntity(UUID id, UUID conversationId, UUID userId, String role, String content, String modelUsed, String citationsJson) {
        this.id = id;
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.content = content;
        this.modelUsed = modelUsed;
        this.citationsJson = citationsJson == null ? "[]" : citationsJson;
    }

    public UUID getId() { return id; }
    public UUID getConversationId() { return conversationId; }
    public UUID getUserId() { return userId; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public String getModelUsed() { return modelUsed; }
    public String getCitationsJson() { return citationsJson; }
    public Instant getCreatedAt() { return createdAt; }
}

