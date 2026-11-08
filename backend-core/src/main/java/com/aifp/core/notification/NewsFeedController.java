package com.aifp.core.notification;

import com.aifp.core.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/news")
public class NewsFeedController {

    private final LiveNewsCrawlerService liveNewsCrawlerService;

    public NewsFeedController(LiveNewsCrawlerService liveNewsCrawlerService) {
        this.liveNewsCrawlerService = liveNewsCrawlerService;
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
            @RequestParam(value = "channel", required = false) String channel) {
        List<LiveArticleEntity> entities = liveNewsCrawlerService.getArticles(channel);
        List<LiveArticleDto> dtos = entities.stream().map(LiveArticleDto::fromEntity).toList();
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

