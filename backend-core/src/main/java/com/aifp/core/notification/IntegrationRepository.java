package com.aifp.core.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IntegrationRepository extends JpaRepository<IntegrationEntity, UUID> {
    List<IntegrationEntity> findByUserIdAndProviderAndStatus(UUID userId, String provider, String status);
    List<IntegrationEntity> findByUserIdAndProviderStartingWithAndStatus(UUID userId, String providerPrefix, String status);
}

