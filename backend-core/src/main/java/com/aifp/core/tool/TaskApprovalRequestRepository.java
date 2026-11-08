package com.aifp.core.tool;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskApprovalRequestRepository extends JpaRepository<TaskApprovalRequestEntity, UUID> {
    List<TaskApprovalRequestEntity> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);
    Optional<TaskApprovalRequestEntity> findByIdAndUserId(UUID id, UUID userId);
}

