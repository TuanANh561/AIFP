package com.aifp.core.opensource;

import com.aifp.core.notification.EmailCenterService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class OpenSourceDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(OpenSourceDiscoveryService.class);

    private final UserOpenSourceRepository repository;
    private final EmailCenterService emailCenterService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenSourceDiscoveryService(
            UserOpenSourceRepository repository,
            EmailCenterService emailCenterService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.emailCenterService = emailCenterService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    public record DiscoveredRepoItem(
            String name,
            String stars,
            String url,
            String tag,
            String summary
    ) {}

    @Transactional
    public List<UserOpenSourceEntity> getOrSeedUserItems(UUID userId, Boolean pinnedOnly) {
        if (Boolean.TRUE.equals(pinnedOnly)) {
            return repository.findByUserIdAndIsPinnedOrderByCreatedAtDesc(userId, true);
        }

        List<UserOpenSourceEntity> existing = repository.findByUserIdOrderByCreatedAtDesc(userId);
        if (!existing.isEmpty()) {
            return existing;
        }

        // Seed 8 top starter tools for newly initiated user library
        List<DiscoveredRepoItem> starters = getCuratedStarterRepos();
        List<UserOpenSourceEntity> seeded = new ArrayList<>();
        for (DiscoveredRepoItem item : starters) {
            UserOpenSourceEntity entity = new UserOpenSourceEntity(
                    UUID.randomUUID(),
                    userId,
                    item.name(),
                    item.stars(),
                    item.url(),
                    item.tag(),
                    item.summary(),
                    "Khởi tạo hệ thống",
                    false
            );
            seeded.add(repository.save(entity));
        }
        return seeded;
    }

    @Transactional
    public List<UserOpenSourceEntity> discoverAndPersist(
            UUID userId,
            String keywords,
            String goal,
            boolean sendEmail,
            String targetEmail
    ) {
        String cleanKeywords = (keywords != null && !keywords.isBlank()) ? keywords.trim() : "ai agent";
        String cleanGoal = (goal != null && !goal.isBlank()) ? goal.trim() : "Khám phá mô hình và công cụ mới";

        log.info("[OpenSourceRadar] Discovering for userId: {} | Keywords: '{}' | Goal: '{}'", userId, cleanKeywords, cleanGoal);

        List<DiscoveredRepoItem> discovered = queryGitHubOrFallback(cleanKeywords);
        List<UserOpenSourceEntity> savedList = new ArrayList<>();

        for (DiscoveredRepoItem item : discovered) {
            Optional<UserOpenSourceEntity> opt = repository.findByUserIdAndUrl(userId, item.url());
            if (opt.isPresent()) {
                UserOpenSourceEntity existing = opt.get();
                existing.setStars(item.stars());
                existing.setSummary(item.summary());
                existing.setQueryKeyword(cleanKeywords);
                savedList.add(repository.save(existing));
            } else {
                UserOpenSourceEntity newEntity = new UserOpenSourceEntity(
                        UUID.randomUUID(),
                        userId,
                        item.name(),
                        item.stars(),
                        item.url(),
                        item.tag(),
                        item.summary(),
                        cleanKeywords,
                        false
                );
                savedList.add(repository.save(newEntity));
            }
        }

        // Send digest email if requested and target email provided
        if (sendEmail && targetEmail != null && !targetEmail.isBlank() && targetEmail.contains("@")) {
            dispatchOpenSourceDigestEmail(targetEmail, cleanKeywords, cleanGoal, discovered);
        }

        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public UserOpenSourceEntity togglePin(UUID userId, UUID itemId) {
        UserOpenSourceEntity entity = repository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mục mã nguồn mở với ID: " + itemId));
        entity.setPinned(!entity.isPinned());
        return repository.save(entity);
    }

    @Transactional
    public boolean deleteItem(UUID userId, UUID itemId) {
        return repository.deleteByIdAndUserId(itemId, userId) > 0;
    }

    @Transactional
    public int clearUnpinned(UUID userId) {
        return repository.deleteByUserIdAndIsPinnedFalse(userId);
    }

    private List<DiscoveredRepoItem> queryGitHubOrFallback(String keywords) {
        List<DiscoveredRepoItem> results = new ArrayList<>();
        try {
            String queryParam = URLEncoder.encode(keywords + " stars:>100", StandardCharsets.UTF_8);
            String url = "https://api.github.com/search/repositories?q=" + queryParam + "&sort=stars&order=desc&per_page=8";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "AIFP-Assistant-Radar/1.0")
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .timeout(Duration.ofSeconds(6))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(resp.body());
                JsonNode items = root.get("items");
                if (items != null && items.isArray() && items.size() > 0) {
                    for (JsonNode item : items) {
                        String fullName = item.path("full_name").asText();
                        int stars = item.path("stargazers_count").asInt(0);
                        String starsFormatted = (stars >= 1000) ? String.format("★ %.1fk", stars / 1000.0) : ("★ " + stars);
                        String htmlUrl = item.path("html_url").asText();
                        String desc = item.path("description").asText("");
                        if (desc.isBlank()) desc = "Kho mã nguồn mở được đề xuất cho từ khóa: " + keywords;
                        String language = item.path("language").asText("Mã nguồn mở");

                        results.add(new DiscoveredRepoItem(
                                fullName,
                                starsFormatted,
                                htmlUrl,
                                language,
                                desc
                        ));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[OpenSourceRadar] GitHub API query error: {} -> using curated pool", e.getMessage());
        }

        if (results.isEmpty()) {
            results = matchCuratedRepos(keywords);
        }
        return results;
    }

    private void dispatchOpenSourceDigestEmail(
            String recipientEmail,
            String keywords,
            String goal,
            List<DiscoveredRepoItem> items
    ) {
        StringBuilder cardsHtml = new StringBuilder();
        for (DiscoveredRepoItem item : items) {
            cardsHtml.append("""
                <div style="background:#FFFFFF;border:1px solid #E2E8F0;border-radius:8px;padding:14px 16px;margin-bottom:12px;">
                  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
                    <a href="%s" style="font-weight:700;font-size:14px;color:#0F172A;text-decoration:none;">%s</a>
                    <span style="font-size:11px;background:#F1F5F9;padding:2px 8px;border-radius:4px;color:#475569;font-weight:600;">%s</span>
                  </div>
                  <div style="font-size:11px;color:#2563EB;font-weight:600;margin-bottom:6px;">%s</div>
                  <p style="margin:0 0 10px 0;font-size:12.5px;color:#475569;line-height:1.45;">%s</p>
                  <a href="%s" style="display:inline-block;padding:5px 12px;background:#0F172A;color:#FFFFFF;border-radius:6px;font-size:11.5px;font-weight:600;text-decoration:none;">Xem trên GitHub &rarr;</a>
                </div>
            """.formatted(item.url(), item.name(), item.stars(), item.tag(), item.summary(), item.url()));
        }

        String content = """
            <div style="margin-bottom:16px;">
              <p style="margin:0 0 6px 0;font-size:13px;color:#334155;"><strong>Từ khóa tìm kiếm:</strong> %s</p>
              <p style="margin:0;font-size:13px;color:#334155;"><strong>Mục tiêu:</strong> %s</p>
            </div>
            %s
            <div style="margin-top:16px;font-size:11.5px;color:#64748B;">
              Toàn bộ các mục trên đã được lưu tự động vào CSDL cá nhân của bạn. Bạn có thể mở ứng dụng để <strong>[Lưu / Ghim]</strong> hoặc <strong>[Xóa]</strong> bất kỳ lúc nào.
            </div>
        """.formatted(keywords, goal, cardsHtml.toString());

        String subject = "Aura · Đề xuất Mã Nguồn Mở Hôm Nay [" + keywords + "]";
        String html = emailCenterService.buildDigestHtml(subject, content);
        emailCenterService.sendRealHtmlEmail(recipientEmail, subject, html);
    }

    private List<DiscoveredRepoItem> matchCuratedRepos(String keywords) {
        String lower = keywords.toLowerCase();
        List<DiscoveredRepoItem> matches = new ArrayList<>();

        if (lower.contains("video") || lower.contains("short") || lower.contains("kịch bản") || lower.contains("phim")) {
            matches.add(new DiscoveredRepoItem("kaomei/stickman-video-director", "★ 4.2k", "https://github.com/kaomei/stickman-video-director", "Video người que kịch bản", "Tự động đạo diễn video người que không lộ mặt cho YouTube & TikTok từ kịch bản phân cảnh."));
            matches.add(new DiscoveredRepoItem("lemomo-ai/lemo-opuscar", "★ 3.8k", "https://github.com/lemomo-ai/lemo-opuscar", "Canvas & WebGL Motion", "Làm phim hoạt hình và đồ họa chuyển động bằng code Canvas/WebGL không cần GPU nặng."));
            matches.add(new DiscoveredRepoItem("mifi/lossless-cut", "★ 28.1k", "https://github.com/mifi/lossless-cut", "Biên tập video mở", "Cắt ghép, tinh chỉnh tỉ lệ 16:9/9:16 và xuất video siêu tốc tức thì không suy hao chất lượng."));
            matches.add(new DiscoveredRepoItem("remotion-dev/remotion", "★ 21.5k", "https://github.com/remotion-dev/remotion", "Video React Pipeline", "Tạo và render video lập trình bằng React và TypeScript tự động hóa 100%."));
        } else if (lower.contains("llm") || lower.contains("slm") || lower.contains("mô hình") || lower.contains("reasoning")) {
            matches.add(new DiscoveredRepoItem("deepseek-ai/DeepSeek-R1", "★ 78.4k", "https://github.com/deepseek-ai/DeepSeek-R1", "Lý luận MoE mã nguồn mở", "Mô hình suy luận mở hiệu năng vượt trội với tư duy chain-of-thought minh bạch."));
            matches.add(new DiscoveredRepoItem("ollama/ollama", "★ 118k", "https://github.com/ollama/ollama", "Offline SLM Runner", "Chạy mô hình ngôn ngữ lớn trực tiếp trên máy cục bộ với 0đ chi phí token API."));
            matches.add(new DiscoveredRepoItem("vllm-project/vllm", "★ 38.2k", "https://github.com/vllm-project/vllm", "High-Throughput Serving", "Thư viện phục vụ suy luận LLM thông lượng cực lớn với kỹ thuật PagedAttention."));
            matches.add(new DiscoveredRepoItem("unslothai/unsloth", "★ 24.3k", "https://github.com/unslothai/unsloth", "Fast LLM Fine-Tuning", "Huấn luyện và fine-tune LLM nhanh hơn 5x và tiết kiệm tới 80% bộ nhớ GPU."));
        } else if (lower.contains("game") || lower.contains("godot")) {
            matches.add(new DiscoveredRepoItem("godotengine/godot", "★ 89.2k", "https://github.com/godotengine/godot", "Game Engine Đa Nền Tảng", "Game engine mã nguồn mở 2D/3D nhẹ và linh hoạt nhất, hỗ trợ C# và GDScript."));
            matches.add(new DiscoveredRepoItem("pixelorama/pixelorama", "★ 7.8k", "https://github.com/Orama-Interactive/Pixelorama", "Pixel Art Editor", "Phần mềm vẽ sprite và hoạt hình pixel 2D mã nguồn mở xây dựng trên Godot."));
        } else {
            matches.addAll(getCuratedStarterRepos());
        }
        return matches;
    }

    private List<DiscoveredRepoItem> getCuratedStarterRepos() {
        return List.of(
                new DiscoveredRepoItem("kaomei/stickman-video-director", "★ 4.2k", "https://github.com/kaomei/stickman-video-director", "Video người que kịch bản", "Tự động đạo diễn video người que không lộ mặt cho YouTube & TikTok từ kịch bản phân cảnh."),
                new DiscoveredRepoItem("lemomo-ai/lemo-opuscar", "★ 3.8k", "https://github.com/lemomo-ai/lemo-opuscar", "Canvas & WebGL Motion", "Làm phim hoạt hình và đồ họa chuyển động bằng code Canvas/WebGL không cần GPU nặng."),
                new DiscoveredRepoItem("mifi/lossless-cut", "★ 28.1k", "https://github.com/mifi/lossless-cut", "Biên tập video mở", "Cắt ghép, tinh chỉnh tỉ lệ 16:9/9:16 và xuất video siêu tốc tức thì không suy hao chất lượng."),
                new DiscoveredRepoItem("unclecode/crawl4ai", "★ 34.5k", "https://github.com/unclecode/crawl4ai", "Thu thập tin & Việc làm", "Cào quét dữ liệu tự động tốc độ cao, trích xuất Markdown sạch không quảng cáo."),
                new DiscoveredRepoItem("modelcontextprotocol/servers", "★ 18.2k", "https://github.com/modelcontextprotocol", "Chuẩn công cụ MCP", "Chuẩn kết nối công cụ mở của Anthropic, tích hợp hơn 1,000 API doanh nghiệp."),
                new DiscoveredRepoItem("mem0ai/mem0", "★ 26.8k", "https://github.com/mem0ai/mem0", "Bộ nhớ dài hạn", "Tự động học thói quen người dùng, giải quyết xung đột thông tin và nén bộ nhớ."),
                new DiscoveredRepoItem("ollama/ollama", "★ 118k", "https://github.com/ollama/ollama", "Offline SLM Runner", "Chạy mô hình ngôn ngữ lớn trực tiếp trên máy cục bộ với 0đ chi phí token API."),
                new DiscoveredRepoItem("livekit/agents", "★ 7.9k", "https://github.com/livekit/agents", "Voice & Realtime Stream", "Xây dựng trợ lý đàm thoại giọng nói song công với độ trễ phản hồi dưới 300ms.")
        );
    }
}
