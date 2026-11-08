package com.aifp.core.pccompanion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PcCompanionDeviceRepository extends JpaRepository<PcCompanionDeviceEntity, UUID> {

    List<PcCompanionDeviceEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

