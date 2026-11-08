package com.aifp.core.tool;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiToolRepository extends JpaRepository<AiToolEntity, UUID> {
    Optional<AiToolEntity> findByCode(String code);
}

