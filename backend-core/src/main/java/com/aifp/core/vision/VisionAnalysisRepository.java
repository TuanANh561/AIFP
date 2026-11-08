package com.aifp.core.vision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisionAnalysisRepository extends JpaRepository<VisionAnalysisEntity, UUID> {

    List<VisionAnalysisEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<VisionAnalysisEntity> findByUserIdAndAnalysisModeOrderByCreatedAtDesc(UUID userId, String analysisMode);
}

