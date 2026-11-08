package com.aifp.core.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SecurityAuditService {

    private final SecurityAuditLogRepository repository;

    public SecurityAuditService(SecurityAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(UUID userId, String eventType, String severity, String resourceType, String resourceId, String metadataJson) {
        SecurityAuditLogEntity entity = new SecurityAuditLogEntity(
                UUID.randomUUID(),
                userId,
                eventType,
                severity,
                resourceType,
                resourceId,
                metadataJson
        );
        repository.save(entity);
    }
}

