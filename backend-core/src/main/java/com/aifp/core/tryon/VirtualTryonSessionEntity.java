package com.aifp.core.tryon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "virtual_tryon_sessions")
public class VirtualTryonSessionEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "garment_title", nullable = false, length = 255)
    private String garmentTitle;

    @Column(name = "garment_category", nullable = false, length = 64)
    private String garmentCategory;

    @Column(name = "garment_image_url", length = 1024)
    private String garmentImageUrl;

    @Column(name = "result_image_url", length = 1024)
    private String resultImageUrl;

    @Column(nullable = false, length = 32)
    private String status = "COMPLETED";

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected VirtualTryonSessionEntity() {}

    public VirtualTryonSessionEntity(
            UUID id,
            UUID userId,
            String garmentTitle,
            String garmentCategory,
            String garmentImageUrl,
            String resultImageUrl,
            Instant expiresAt
    ) {
        this.id = id;
        this.userId = userId;
        this.garmentTitle = garmentTitle;
        this.garmentCategory = garmentCategory;
        this.garmentImageUrl = garmentImageUrl;
        this.resultImageUrl = resultImageUrl;
        this.status = "COMPLETED";
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getGarmentTitle() { return garmentTitle; }
    public String getGarmentCategory() { return garmentCategory; }
    public String getGarmentImageUrl() { return garmentImageUrl; }
    public String getResultImageUrl() { return resultImageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}

