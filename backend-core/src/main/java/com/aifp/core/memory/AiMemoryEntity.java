package com.aifp.core.memory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_memories")
public class AiMemoryEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String category;

    @Column(name = "topic_key")
    private String topicKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "embedding_json", columnDefinition = "TEXT")
    private String embeddingJson;

    @Column(nullable = false)
    private String status = "ACTIVE"; // CANDIDATE, ACTIVE, SUPERSEDED, DELETED

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal confidence = BigDecimal.ONE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public AiMemoryEntity() {
    }

    public AiMemoryEntity(UUID id, UUID userId, String category, String topicKey, String content, String status) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.topicKey = topicKey;
        this.content = content;
        this.status = status != null ? status : "ACTIVE";
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getCategory() { return category; }
    public String getTopicKey() { return topicKey; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

