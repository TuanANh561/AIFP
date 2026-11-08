package com.aifp.core.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskExecutionRepository extends JpaRepository<TaskExecutionEntity, UUID> {
    Optional<TaskExecutionEntity> findByTaskIdAndIdempotencyKey(UUID taskId, String idempotencyKey);
    List<TaskExecutionEntity> findTop30ByTaskIdAndUserIdOrderByStartedAtDesc(UUID taskId, UUID userId);
}

