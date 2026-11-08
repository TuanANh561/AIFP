package com.aifp.core.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calendar_events")
public class CalendarEventEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "approval_request_id")
    private UUID approvalRequestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CalendarEventEntity() {}

    public CalendarEventEntity(
            UUID id,
            UUID userId,
            String title,
            Instant startTime,
            Instant endTime,
            String location,
            String description,
            String status,
            UUID approvalRequestId
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;
        this.status = status;
        this.approvalRequestId = approvalRequestId;
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

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getApprovalRequestId() {
        return approvalRequestId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

