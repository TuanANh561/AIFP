package com.aifp.core.writing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "writing_drafts")
public class WritingDraftEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "doc_type", nullable = false, length = 40)
    private String docType;

    @Column(nullable = false, length = 40)
    private String tone;

    @Column(name = "prompt_brief", nullable = false, columnDefinition = "TEXT")
    private String promptBrief;

    @Column(name = "content_markdown", nullable = false, columnDefinition = "TEXT")
    private String contentMarkdown;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WritingDraftEntity() {}

    public WritingDraftEntity(
            UUID id,
            UUID userId,
            String title,
            String docType,
            String tone,
            String promptBrief,
            String contentMarkdown
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.docType = docType;
        this.tone = tone;
        this.promptBrief = promptBrief;
        this.contentMarkdown = contentMarkdown;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDocType() {
        return docType;
    }

    public String getTone() {
        return tone;
    }

    public String getPromptBrief() {
        return promptBrief;
    }

    public String getContentMarkdown() {
        return contentMarkdown;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

