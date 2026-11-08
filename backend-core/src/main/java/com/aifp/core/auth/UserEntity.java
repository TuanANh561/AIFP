package com.aifp.core.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 1;

    @Column(name = "daily_token_quota", nullable = false)
    private int dailyTokenQuota = 150000;

    @Column(name = "daily_tokens_used", nullable = false)
    private int dailyTokensUsed = 0;

    @Column(name = "quota_reset_at", nullable = false)
    private Instant quotaResetAt = Instant.now();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserEntity() {
    }

    public UserEntity(UUID id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(int tokenVersion) { this.tokenVersion = tokenVersion; }
    public int getDailyTokenQuota() { return dailyTokenQuota; }
    public void setDailyTokenQuota(int dailyTokenQuota) { this.dailyTokenQuota = dailyTokenQuota; }
    public int getDailyTokensUsed() { return dailyTokensUsed; }
    public void setDailyTokensUsed(int dailyTokensUsed) { this.dailyTokensUsed = dailyTokensUsed; }
    public Instant getQuotaResetAt() { return quotaResetAt; }
    public void setQuotaResetAt(Instant quotaResetAt) { this.quotaResetAt = quotaResetAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

