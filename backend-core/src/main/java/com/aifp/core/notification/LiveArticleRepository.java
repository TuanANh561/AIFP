package com.aifp.core.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveArticleRepository extends JpaRepository<LiveArticleEntity, UUID> {
    boolean existsByUrl(String url);
    Optional<LiveArticleEntity> findByUrl(String url);
    List<LiveArticleEntity> findAllByOrderByPublishedAtDesc();
    List<LiveArticleEntity> findTop30ByOrderByPublishedAtDesc();
    List<LiveArticleEntity> findByChannelIdOrderByPublishedAtDesc(String channelId);
}

