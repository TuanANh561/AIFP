package com.aifp.core.memory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiMemoryRepository extends JpaRepository<AiMemoryEntity, UUID> {
    List<AiMemoryEntity> findByUserIdAndStatusInOrderByUpdatedAtDesc(UUID userId, List<String> statuses);
    Optional<AiMemoryEntity> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE AiMemoryEntity m SET m.status = 'SUPERSEDED' WHERE m.userId = :userId AND m.topicKey = :topicKey AND m.status = 'ACTIVE'")
    int supersedeActiveMemoriesByTopic(@Param("userId") UUID userId, @Param("topicKey") String topicKey);
}

