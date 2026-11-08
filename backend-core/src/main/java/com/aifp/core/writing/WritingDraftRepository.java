package com.aifp.core.writing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WritingDraftRepository extends JpaRepository<WritingDraftEntity, UUID> {
    List<WritingDraftEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<WritingDraftEntity> findByIdAndUserId(UUID id, UUID userId);
}

