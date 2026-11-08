package com.aifp.core.shopping;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItemEntity, UUID> {
    List<ShoppingItemEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<ShoppingItemEntity> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);
    Optional<ShoppingItemEntity> findByIdAndUserId(UUID id, UUID userId);
}

