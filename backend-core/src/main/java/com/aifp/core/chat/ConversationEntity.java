package com.aifp.core.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_conversations")
public class ConversationEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "agent_id")
    private UUID agentId;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned = false;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt = Instant.now();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ConversationEntity() {
    }

    public ConversationEntity(UUID id, UUID userId, String title) {
        this.id = id;
        this.userId = userId;
        this.title = title;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public Instant getCreatedAt() { return createdAt; }
}

