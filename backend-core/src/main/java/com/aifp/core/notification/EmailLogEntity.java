package com.aifp.core.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_logs")
public class EmailLogEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "execution_id")
    private UUID executionId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(nullable = false)
    private String status; // SENT, SKIPPED_DUPLICATE, FAILED

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt = Instant.now();

    public EmailLogEntity() {
    }

    public EmailLogEntity(UUID id, UUID userId, UUID executionId, String recipientEmail, String subject, String providerMessageId, String status) {
        this.id = id;
        this.userId = userId;
        this.executionId = executionId;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.providerMessageId = providerMessageId;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getExecutionId() { return executionId; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }
}

