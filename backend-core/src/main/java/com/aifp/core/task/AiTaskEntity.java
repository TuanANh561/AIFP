package com.aifp.core.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_tasks")
public class AiTaskEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "natural_language_prompt", nullable = false, columnDefinition = "TEXT")
    private String naturalLanguagePrompt;

    @Column(name = "task_type", nullable = false)
    private String taskType = "RESEARCH_MONITOR";

    @Column(name = "structured_config_json", nullable = false, columnDefinition = "TEXT")
    private String structuredConfigJson = "{}";

    @Column(name = "allowed_tools_json", nullable = false, columnDefinition = "TEXT")
    private String allowedToolsJson = "[\"searxng_search\",\"webpage_extract\"]";

    @Column(name = "delivery_mode", nullable = false)
    private String deliveryMode = "DELTA_ONLY";

    @Column(name = "delivery_channels_json", nullable = false, columnDefinition = "TEXT")
    private String deliveryChannelsJson = "[\"IN_APP\",\"EMAIL\",\"WEB_PUSH\"]";

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public AiTaskEntity() {
    }

    public AiTaskEntity(UUID id, UUID userId, String title, String naturalLanguagePrompt, String deliveryMode) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.naturalLanguagePrompt = naturalLanguagePrompt;
        this.deliveryMode = deliveryMode != null ? deliveryMode : "DELTA_ONLY";
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getNaturalLanguagePrompt() { return naturalLanguagePrompt; }
    public String getTaskType() { return taskType; }
    public String getDeliveryMode() { return deliveryMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

