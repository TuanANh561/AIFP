package com.aifp.core.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integrations")
public class IntegrationEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String provider; // WEB_PUSH_SUBSCRIPTION, GOOGLE_CALENDAR

    @Column(name = "encrypted_credentials", nullable = false)
    private byte[] encryptedCredentials;

    @Column(name = "key_version", nullable = false)
    private short keyVersion = 1;

    @Column(name = "scopes_json", nullable = false)
    private String scopesJson = "[]";

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public IntegrationEntity() {
    }

    public IntegrationEntity(UUID id, UUID userId, String provider, byte[] encryptedCredentials) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.encryptedCredentials = encryptedCredentials;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getProvider() { return provider; }
    public byte[] getEncryptedCredentials() { return encryptedCredentials; }
    public String getStatus() { return status; }
}

