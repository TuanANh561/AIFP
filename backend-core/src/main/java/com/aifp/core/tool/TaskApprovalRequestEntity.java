package com.aifp.core.tool;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_approval_requests")
public class TaskApprovalRequestEntity {

    @Id
    private UUID id;

    @Column(name = "execution_id")
    private UUID executionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "tool_code", nullable = false)
    private String toolCode;

    @Column(name = "proposed_payload_json", nullable = false)
    private String proposedPayloadJson;

    @Column(name = "langgraph_checkpoint_json", nullable = false)
    private String langgraphCheckpointJson;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public TaskApprovalRequestEntity() {
    }

    public TaskApprovalRequestEntity(
            UUID id,
            UUID executionId,
            UUID userId,
            String toolCode,
            String proposedPayloadJson,
            String langgraphCheckpointJson,
            Instant expiresAt
    ) {
        this.id = id;
        this.executionId = executionId;
        this.userId = userId;
        this.toolCode = toolCode;
        this.proposedPayloadJson = proposedPayloadJson;
        this.langgraphCheckpointJson = langgraphCheckpointJson;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getToolCode() { return toolCode; }
    public String getProposedPayloadJson() { return proposedPayloadJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}

