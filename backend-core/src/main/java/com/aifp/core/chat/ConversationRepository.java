package com.aifp.core.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {
    List<ConversationEntity> findByUserIdOrderByLastMessageAtDesc(UUID userId);
    Optional<ConversationEntity> findByIdAndUserId(UUID id, UUID userId);
}

