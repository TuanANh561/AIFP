package com.aifp.core.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_audit_logs")
public class SecurityAuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String severity = "INFO";

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "metadata_json", nullable = false)
    private String metadataJson = "{}";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public SecurityAuditLogEntity() {
    }

    public SecurityAuditLogEntity(UUID id, UUID userId, String eventType, String severity, String resourceType, String resourceId, String metadataJson) {
        this.id = id;
        this.userId = userId;
        this.eventType = eventType;
        this.severity = severity;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.metadataJson = metadataJson == null ? "{}" : metadataJson;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public String getSeverity() { return severity; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public Instant getCreatedAt() { return createdAt; }
}

