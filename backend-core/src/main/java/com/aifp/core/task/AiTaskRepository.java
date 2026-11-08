package com.aifp.core.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiTaskRepository extends JpaRepository<AiTaskEntity, UUID> {
    List<AiTaskEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<AiTaskEntity> findByStatus(String status);
    Optional<AiTaskEntity> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE AiTaskEntity t SET t.status = 'PAUSED_BY_KILL_SWITCH' WHERE t.userId = :userId AND t.status = 'ACTIVE'")
    int pauseAllActiveTasksForKillSwitch(@Param("userId") UUID userId);
}

