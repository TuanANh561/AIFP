package com.aifp.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, UUID> {
    List<CalendarEventEntity> findByUserIdOrderByStartTimeAsc(UUID userId);
    Optional<CalendarEventEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<CalendarEventEntity> findByApprovalRequestIdAndUserId(UUID approvalRequestId, UUID userId);
}

