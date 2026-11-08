package com.aifp.core.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
    List<MessageEntity> findByConversationIdAndUserIdOrderByCreatedAtAsc(UUID conversationId, UUID userId);
}

