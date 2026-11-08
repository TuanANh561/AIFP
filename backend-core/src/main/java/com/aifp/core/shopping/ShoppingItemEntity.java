package com.aifp.core.shopping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shopping_tracked_items")
public class ShoppingItemEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "target_price", precision = 15, scale = 2)
    private BigDecimal targetPrice;

    @Column(name = "current_price", precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, length = 10)
    private String currency = "VND";

    @Column(name = "store_url", nullable = false, length = 1024)
    private String storeUrl;

    @Column(name = "store_name", length = 120)
    private String storeName;

    @Column(nullable = false, length = 32)
    private String status = "TRACKING";

    @Column(name = "last_checked_at", nullable = false)
    private Instant lastCheckedAt = Instant.now();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected ShoppingItemEntity() {}

    public ShoppingItemEntity(
            UUID id,
            UUID userId,
            String title,
            BigDecimal targetPrice,
            BigDecimal currentPrice,
            String currency,
            String storeUrl,
            String storeName
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.targetPrice = targetPrice;
        this.currentPrice = currentPrice;
        this.currency = (currency != null && !currency.isBlank()) ? currency.trim().toUpperCase() : "VND";
        this.storeUrl = storeUrl;
        this.storeName = storeName;
        this.status = "TRACKING";
        this.lastCheckedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public BigDecimal getTargetPrice() { return targetPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public String getCurrency() { return currency; }
    public String getStoreUrl() { return storeUrl; }
    public String getStoreName() { return storeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public Instant getCreatedAt() { return createdAt; }
}

