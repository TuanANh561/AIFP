package com.aifp.core.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_executions")
public class TaskExecutionEntity {

    @Id
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "trigger_type", nullable = false)
    private String triggerType = "SCHEDULED";

    @Column(nullable = false)
    private String status = "QUEUED";

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 1;

    @Column(name = "step_checkpoints_json", nullable = false, columnDefinition = "TEXT")
    private String stepCheckpointsJson = "{}";

    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    @Column(name = "result_items_json", nullable = false, columnDefinition = "TEXT")
    private String resultItemsJson = "[]";

    @Column(name = "content_fingerprints_json", nullable = false, columnDefinition = "TEXT")
    private String contentFingerprintsJson = "[]";

    @Column(name = "verified_citations_json", nullable = false, columnDefinition = "TEXT")
    private String verifiedCitationsJson = "[]";

    @Column(name = "tokens_consumed", nullable = false)
    private int tokensConsumed = 0;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public TaskExecutionEntity() {
    }

    public TaskExecutionEntity(UUID id, UUID taskId, UUID userId, String idempotencyKey, String triggerType, String status) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.triggerType = triggerType;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getTaskId() { return taskId; }
    public UUID getUserId() { return userId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    public String getContentFingerprintsJson() { return contentFingerprintsJson; }
    public void setContentFingerprintsJson(String contentFingerprintsJson) { this.contentFingerprintsJson = contentFingerprintsJson; }
    public String getVerifiedCitationsJson() { return verifiedCitationsJson; }
    public void setVerifiedCitationsJson(String verifiedCitationsJson) { this.verifiedCitationsJson = verifiedCitationsJson; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

