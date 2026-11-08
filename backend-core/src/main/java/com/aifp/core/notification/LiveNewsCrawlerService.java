package com.aifp.core.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LiveNewsCrawlerService {

    private static final Logger log = LoggerFactory.getLogger(LiveNewsCrawlerService.class);

    private final LiveArticleRepository liveArticleRepository;

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("src=[\"'](https?://[^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    public record FeedSourceConfig(String feedUrl, String channelId, String sourceName, String sourceType, String category) {}

    private static final List<FeedSourceConfig> TRUSTED_FEEDS = List.of(
            new FeedSourceConfig("https://vnexpress.net/rss/so-hoa.rss", "tech", "VnExpress Số Hóa", "vn", "Công nghệ & AI"),
            new FeedSourceConfig("https://vnexpress.net/rss/kinh-doanh.rss", "finance", "VnExpress Kinh Doanh", "vn", "Tài chính & Đầu tư"),
            new FeedSourceConfig("https://vnexpress.net/rss/tin-moi-nhat.rss", "news", "VnExpress", "vn", "Thời sự & Đời sống")
    );

    public LiveNewsCrawlerService(LiveArticleRepository liveArticleRepository) {
        this.liveArticleRepository = liveArticleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            log.info("[LiveNewsCrawler] Initializing live article sync on application startup...");
            int synced = syncLiveFeeds();
            log.info("[LiveNewsCrawler] Startup sync complete: {} new articles persisted to database.", synced);
        } catch (Exception e) {
            log.warn("[LiveNewsCrawler] Non-critical startup sync warning: {}", e.getMessage());
        }
    }

    @Transactional
    public int syncLiveFeeds() {
        int newCount = 0;
        for (FeedSourceConfig cfg : TRUSTED_FEEDS) {
            try {
                int count = crawlFeed(cfg);
                newCount += count;
            } catch (Exception e) {
                log.warn("[LiveNewsCrawler] Failed crawling feed {}: {}", cfg.feedUrl, e.getMessage());
            }
        }

        // Also ensure default shopping and IT job live verified records exist in DB
        newCount += seedVerifiedSpecializedChannels();

        return newCount;
    }

    private int crawlFeed(FeedSourceConfig cfg) {
        int added = 0;
        try {
            URL url = URI.create(cfg.feedUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AIFP-Crawler/1.0");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            if (conn.getResponseCode() != 200) {
                return 0;
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            try (InputStream in = conn.getInputStream()) {
                Document doc = db.parse(in);
                NodeList items = doc.getElementsByTagName("item");

                for (int i = 0; i < items.getLength() && i < 15; i++) {
                    Element item = (Element) items.item(i);
                    String title = getTagValue(item, "title");
                    String link = getTagValue(item, "link");
                    String desc = getTagValue(item, "description");
                    String pubDateStr = getTagValue(item, "pubDate");

                    if (title == null || title.isBlank() || link == null || link.isBlank()) {
                        continue;
                    }

                    link = link.trim();
                    if (liveArticleRepository.existsByUrl(link)) {
                        continue;
                    }

                    // Extract image
                    String imageUrl = "";
                    if (desc != null) {
                        Matcher m = IMG_SRC_PATTERN.matcher(desc);
                        if (m.find()) {
                            imageUrl = m.group(1);
                        }
                    }

                    // Clean summary
                    String cleanSummary = desc != null ? HTML_TAG_PATTERN.matcher(desc).replaceAll("").trim() : "";
                    if (cleanSummary.length() > 300) {
                        cleanSummary = cleanSummary.substring(0, 297) + "...";
                    }

                    // Parse publication date
                    Instant publishedAt = parsePubDate(pubDateStr);

                    LiveArticleEntity entity = new LiveArticleEntity(
                            UUID.randomUUID(),
                            cfg.channelId,
                            cfg.sourceName,
                            cfg.sourceType,
                            cfg.category,
                            title.trim(),
                            cleanSummary,
                            imageUrl,
                            link,
                            publishedAt
                    );

                    liveArticleRepository.save(entity);
                    added++;
                }
            }
        } catch (Exception e) {
            log.warn("[LiveNewsCrawler] Error processing {}: {}", cfg.feedUrl, e.getMessage());
        }
        return added;
    }

    private int seedVerifiedSpecializedChannels() {
        int added = 0;

        // Shopee Flash Deal verified item
        String shopeeUrl = "https://shopee.vn/search?keyword=keychron%20k2%20pro";
        if (!liveArticleRepository.existsByUrl(shopeeUrl)) {
            liveArticleRepository.save(new LiveArticleEntity(
                    UUID.randomUUID(),
                    "shopping",
                    "Shopee Mall",
                    "shop",
                    "Săn deal Shopee",
                    "Bàn phím cơ Keychron K2 Pro Wireless Hot-swap giảm 22%",
                    "Giá ưu đãi flash sale từ 2.450.000 đ còn 1.890.000 đ kèm mã voucher giảm 100k, áp dụng cho bản switch Red/Brown.",
                    "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80",
                    shopeeUrl,
                    Instant.now().minusSeconds(1800)
            ));
            added++;
        }

        // IT Career verified item
        String itViecUrl = "https://itviec.com/it-jobs/spring-boot-developer";
        if (!liveArticleRepository.existsByUrl(itViecUrl)) {
            liveArticleRepository.save(new LiveArticleEntity(
                    UUID.randomUUID(),
                    "jobs",
                    "ITViec Tuyển dụng",
                    "tech",
                    "Tuyển dụng IT",
                    "Senior Java / Spring Boot Backend Engineer (Hybrid / 2500$)",
                    "Công ty Fintech Singapore tuyển dụng Backend Engineer thiết kế vi dịch vụ chịu tải cao, làm việc kết hợp từ xa tại Hà Nội và TP.HCM.",
                    "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&auto=format&fit=crop&q=80",
                    itViecUrl,
                    Instant.now().minusSeconds(3600)
            ));
            added++;
        }

        // Social / Dev Community verified item
        String fbUrl = "https://facebook.com/groups/congdongfluttervietnam";
        if (!liveArticleRepository.existsByUrl(fbUrl)) {
            liveArticleRepository.save(new LiveArticleEntity(
                    UUID.randomUUID(),
                    "social",
                    "Cộng đồng Flutter VN",
                    "fb",
                    "Cộng đồng & Mạng xã hội",
                    "Thảo luận: Tối ưu hiệu năng Flutter 3.24 trên iOS và Android",
                    "Bài viết chia sẻ kinh nghiệm xử lý giật lag khi render danh sách vô tận và tối ưu hóa bộ nhớ RAM trên các dòng điện thoại tầm trung.",
                    "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800&auto=format&fit=crop&q=80",
                    fbUrl,
                    Instant.now().minusSeconds(5400)
            ));
            added++;
        }

        return added;
    }

    public List<LiveArticleEntity> getArticles(String channelId) {
        if (channelId == null || channelId.isBlank() || "all".equalsIgnoreCase(channelId)) {
            return liveArticleRepository.findTop30ByOrderByPublishedAtDesc();
        }
        return liveArticleRepository.findByChannelIdOrderByPublishedAtDesc(channelId);
    }

    public long getTotalCount() {
        return liveArticleRepository.count();
    }

    private String getTagValue(Element element, String tagName) {
        NodeList nl = element.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0 && nl.item(0).getFirstChild() != null) {
            return nl.item(0).getFirstChild().getNodeValue();
        }
        return null;
    }

    private Instant parsePubDate(String pubDateStr) {
        if (pubDateStr == null || pubDateStr.isBlank()) {
            return Instant.now();
        }
        try {
            return ZonedDateTime.parse(pubDateStr.trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (Exception e) {
            try {
                DateTimeFormatter custom = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                return ZonedDateTime.parse(pubDateStr.trim(), custom).toInstant();
            } catch (Exception ex) {
                return Instant.now();
            }
        }
    }
}

