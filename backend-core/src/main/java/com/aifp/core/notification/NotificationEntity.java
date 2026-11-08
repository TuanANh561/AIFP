package com.aifp.core.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "execution_id")
    private UUID executionId;

    @Column(nullable = false)
    private String type; // TASK_RESULT, APPROVAL_NEEDED, SECURITY_ALERT

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "web_push_sent", nullable = false)
    private boolean webPushSent = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public NotificationEntity() {
    }

    public NotificationEntity(UUID id, UUID userId, UUID executionId, String type, String title, String body, boolean webPushSent) {
        this.id = id;
        this.userId = userId;
        this.executionId = executionId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.webPushSent = webPushSent;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getExecutionId() { return executionId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public boolean isWebPushSent() { return webPushSent; }
    public Instant getCreatedAt() { return createdAt; }
}

