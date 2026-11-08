package com.aifp.core.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailLogRepository extends JpaRepository<EmailLogEntity, UUID> {
    Optional<EmailLogEntity> findByExecutionIdAndRecipientEmail(UUID executionId, String recipientEmail);
    List<EmailLogEntity> findByUserIdOrderBySentAtDesc(UUID userId);
}

