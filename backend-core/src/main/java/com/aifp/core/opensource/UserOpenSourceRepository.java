package com.aifp.core.opensource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOpenSourceRepository extends JpaRepository<UserOpenSourceEntity, UUID> {

    List<UserOpenSourceEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<UserOpenSourceEntity> findByUserIdAndIsPinnedOrderByCreatedAtDesc(UUID userId, boolean isPinned);

    Optional<UserOpenSourceEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<UserOpenSourceEntity> findByUserIdAndUrl(UUID userId, String url);

    boolean existsByUserIdAndUrl(UUID userId, String url);

    @Modifying
    @Query("DELETE FROM UserOpenSourceEntity u WHERE u.id = :id AND u.userId = :userId")
    int deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM UserOpenSourceEntity u WHERE u.userId = :userId AND u.isPinned = false")
    int deleteByUserIdAndIsPinnedFalse(@Param("userId") UUID userId);
}

