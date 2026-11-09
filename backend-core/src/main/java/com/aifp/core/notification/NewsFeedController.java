package com.aifp.core.notification;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/news")
public class NewsFeedController {

    private final LiveNewsCrawlerService liveNewsCrawlerService;
    private final String ownerEmail;

    public NewsFeedController(
            LiveNewsCrawlerService liveNewsCrawlerService,
            @Value("${aifp.owner-email:nguyentuananh11a1.lhpt@gmail.com}") String ownerEmail
    ) {
        this.liveNewsCrawlerService = liveNewsCrawlerService;
        this.ownerEmail = (ownerEmail != null && !ownerEmail.isBlank()) ? ownerEmail.trim() : "nguyentuananh11a1.lhpt@gmail.com";
    }

    public record LiveArticleDto(
            UUID id,
            String channelId,
            String source,
            String sourceType,
            String category,
            String title,
            String summary,
            String imageUrl,
            String url,
            Instant publishedAt,
            Instant fetchedAt
    ) {
        public static LiveArticleDto fromEntity(LiveArticleEntity entity) {
            return new LiveArticleDto(
                    entity.getId(),
                    entity.getChannelId(),
                    entity.getSource(),
                    entity.getSourceType(),
                    entity.getCategory(),
                    entity.getTitle(),
                    entity.getSummary(),
                    entity.getImageUrl(),
                    entity.getUrl(),
                    entity.getPublishedAt(),
                    entity.getFetchedAt()
            );
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LiveArticleDto>>> getLiveNews(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "channel", required = false) String channel
    ) {
        List<LiveArticleEntity> entities = liveNewsCrawlerService.getArticles(channel);
        boolean isOwner = principal != null && principal.email() != null &&
                principal.email().trim().equalsIgnoreCase(ownerEmail.trim());

        List<LiveArticleDto> dtos = entities.stream()
                .filter(e -> isOwner || (!"ai-evolution".equalsIgnoreCase(e.getChannelId()) && !"creator-ideas".equalsIgnoreCase(e.getChannelId())))
                .map(LiveArticleDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @PostMapping("/sync-live")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncLiveNews() {
        int newAdded = liveNewsCrawlerService.syncLiveFeeds();
        long total = liveNewsCrawlerService.getTotalCount();
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "newAddedCount", newAdded,
                "totalInDatabase", total,
                "syncedAt", Instant.now().toString(),
                "status", "DATABASE_UPDATED"
        )));
    }
}

