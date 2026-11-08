package com.aifp.core.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLogEntity, UUID> {
    List<SecurityAuditLogEntity> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
}

