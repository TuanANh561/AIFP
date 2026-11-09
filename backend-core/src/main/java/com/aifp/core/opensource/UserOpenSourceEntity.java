package com.aifp.core.opensource;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_open_source_items", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_open_source_url", columnNames = {"user_id", "url"})
})
public class UserOpenSourceEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 64)
    private String stars;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(length = 128)
    private String tag;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "query_keyword", length = 255)
    private String queryKeyword;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UserOpenSourceEntity() {
    }

    public UserOpenSourceEntity(UUID id, UUID userId, String name, String stars, String url,
                                String tag, String summary, String queryKeyword, boolean isPinned) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.stars = stars;
        this.url = url;
        this.tag = tag;
        this.summary = summary;
        this.queryKeyword = queryKeyword;
        this.isPinned = isPinned;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getQueryKeyword() {
        return queryKeyword;
    }

    public void setQueryKeyword(String queryKeyword) {
        this.queryKeyword = queryKeyword;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
