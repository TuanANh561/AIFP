package com.aifp.core.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_articles")
public class LiveArticleEntity {

    @Id
    private UUID id;

    @Column(name = "channel_id", nullable = false, length = 64)
    private String channelId;

    @Column(nullable = false, length = 128)
    private String source;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(nullable = false, length = 128)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(nullable = false, unique = true, length = 1024)
    private String url;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt = Instant.now();

    public LiveArticleEntity() {
    }

    public LiveArticleEntity(UUID id, String channelId, String source, String sourceType, String category,
                             String title, String summary, String imageUrl, String url, Instant publishedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.channelId = channelId;
        this.source = source;
        this.sourceType = sourceType;
        this.category = category;
        this.title = title;
        this.summary = summary;
        this.imageUrl = imageUrl;
        this.url = url;
        this.publishedAt = publishedAt != null ? publishedAt : Instant.now();
        this.fetchedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}

