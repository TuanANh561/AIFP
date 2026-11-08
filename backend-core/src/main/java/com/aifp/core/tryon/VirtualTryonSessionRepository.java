package com.aifp.core.tryon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualTryonSessionRepository extends JpaRepository<VirtualTryonSessionEntity, UUID> {

    List<VirtualTryonSessionEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

