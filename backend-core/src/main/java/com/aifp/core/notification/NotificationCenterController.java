package com.aifp.core.notification;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationCenterController {

    private final NotificationCenterService notificationService;
    private final EmailCenterService emailCenterService;

    public NotificationCenterController(
            NotificationCenterService notificationService,
            EmailCenterService emailCenterService
    ) {
        this.notificationService = notificationService;
        this.emailCenterService = emailCenterService;
    }

    public record WebPushSubscriptionRequest(
            @NotBlank String endpoint,
            @NotBlank String p256dh,
            @NotBlank String auth
    ) {}

    public record SendTestDigestRequest(
            @NotBlank @Email String recipientEmail,
            String personaName,
            List<String> categories
    ) {}

    public record SendChannelDigestRequest(
            @NotBlank @Email String recipientEmail,
            @NotBlank String channelId,
            String channelTitle,
            List<String> keywords,
            String scheduledTime
    ) {}

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationEntity>>> listNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.listNotifications(principal.userId())));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationEntity>> markRead(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID notificationId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.markAsRead(principal.userId(), notificationId)
        ));
    }

    @PostMapping("/web-push/subscribe")
    public ResponseEntity<ApiResponse<UUID>> subscribeWebPush(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody WebPushSubscriptionRequest request
    ) {
        IntegrationEntity saved = notificationService.registerWebPushSubscription(
                principal.userId(),
                request.endpoint(),
                request.p256dh(),
                request.auth()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved.getId()));
    }

    @PostMapping("/send-test-digest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendTestDigest(
            @Valid @RequestBody SendTestDigestRequest request
    ) {
        List<EmailCenterService.DigestArticleCard> sampleArticles = new ArrayList<>();
        List<String> cats = request.categories() != null && !request.categories().isEmpty()
                ? request.categories()
                : List.of("TECH", "JOBS", "SHOPPING");

        if (cats.contains("TECH") || cats.contains("AI")) {
            sampleArticles.add(new EmailCenterService.DigestArticleCard(
                    "VnExpress Số Hóa",
                    "tech",
                    "Công nghệ Bluetooth mới cho phép phát âm thanh lossless",
                    "Tổ chức Bluetooth SIG công bố chuẩn kết nối Bluetooth HDT với tốc độ tối đa 7,5 Mb/giây, hỗ trợ phát nhạc lossless chất lượng cao trực tiếp không nén.",
                    "https://vnexpress.net/cong-nghe-bluetooth-moi-cho-phep-phat-am-thanh-lossless-5141993.html",
                    "https://i1-vnexpress.vnecdn.net/2026/11/07/dsc8652-1794060485-2338-1794060541.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=eQVmaEeWfpBX2NhCWVOjkQ",
                    "07:30 Sáng nay"
            ));
        }

        if (cats.contains("JOBS") || cats.contains("CAREER")) {
            sampleArticles.add(new EmailCenterService.DigestArticleCard(
                    "Tuyển dụng ITViec",
                    "jobs",
                    "Tuyển dụng Kỹ sư Lập trình Flutter & Mobile App (iOS / Android)",
                    "Doanh nghiệp công nghệ tuyển dụng chuyên gia Flutter, yêu cầu vững State Management (Riverpod/Bloc), kết nối Clean Architecture và tối ưu hiệu năng PWA.",
                    "https://itviec.com/it-jobs/flutter",
                    "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=1200&auto=format&fit=crop&q=80",
                    "06:30 Sáng nay"
            ));
        }

        if (cats.contains("SHOPPING") || cats.contains("DEALS")) {
            sampleArticles.add(new EmailCenterService.DigestArticleCard(
                    "Shopee Mall",
                    "shop",
                    "Bàn phím cơ Keychron K2 Pro không dây giảm giá còn 1.890.000 đ",
                    "Đã đạt mức giá mục tiêu bạn theo dõi (tiết kiệm 160.000 đ so với giá thị trường), hỗ trợ bảo hành chính hãng 12 tháng và miễn phí vận chuyển.",
                    "https://shopee.vn/search?keyword=keychron+k2+pro",
                    "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=1200&auto=format&fit=crop&q=80",
                    "06:00 Sáng nay"
            ));
        }

        if (cats.contains("FINANCE") || cats.contains("INVESTMENT")) {
            sampleArticles.add(new EmailCenterService.DigestArticleCard(
                    "VnExpress Kinh Doanh",
                    "news",
                    "Nhà đầu tư ngờ vực mức định giá 2.000 tỷ USD của Anthropic",
                    "Sau phiên điều chỉnh cổ phiếu công nghệ tại Mỹ, giới đầu tư bắt đầu cân nhắc chi phí vận hành khổng lồ của các trung tâm dữ liệu AI so với doanh thu thực tế.",
                    "https://vnexpress.net/nha-dau-tu-ngo-vuc-muc-dinh-gia-2-000-ty-usd-cua-anthropic-5142087.html",
                    "https://i1-kinhdoanh.vnecdn.net/2026/11/08/afp-hl-jmbarrere-v1-highres-cl-9797-5057-1794104920.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=7j1J3f3dWaJR8UMdREqVJQ",
                    "06:50 Sáng nay"
            ));
        }

        if (cats.contains("SOCIAL") || cats.contains("NEWS")) {
            sampleArticles.add(new EmailCenterService.DigestArticleCard(
                    "VnExpress Công nghệ",
                    "vn",
                    "iPhone 20 có thể bỏ nút Action và giữ nguyên Dynamic Island",
                    "Apple lên kế hoạch gộp nút Action vào cụm điều khiển Camera Control, giữ Dynamic Island đồng thời tối ưu hóa không gian cho hệ thống pin thế hệ mới.",
                    "https://vnexpress.net/iphone-20-co-the-bo-nut-action-va-giu-nguyen-dynamic-island-5141990.html",
                    "https://i1-vnexpress.vnecdn.net/2026/11/07/fdnnyswsv5m3lw8bs4geuy-1794060-2723-9452-1794060076.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=AiQfW9d6L8Mn5AJ1XM6E9A",
                    "07:15 Sáng nay"
            ));
        }

        String persona = request.personaName() != null && !request.personaName().isBlank()
                ? request.personaName()
                : "Người dùng AIFP";

        String title = "Bản tin cá nhân chọn lọc dành riêng cho " + persona;
        String htmlBody = emailCenterService.renderCardBasedDigestHtml(title, sampleArticles);

        boolean delivered = emailCenterService.sendRealHtmlEmail(
                request.recipientEmail(),
                "[AIFP] " + title,
                htmlBody
        );

        Map<String, Object> responseData = Map.of(
                "delivered", delivered,
                "recipientEmail", request.recipientEmail(),
                "articlesCount", sampleArticles.size(),
                "fromSystemEmail", "nguyentuananh11a1.lhpt@gmail.com",
                "message", delivered
                        ? "Đã gửi bản tin dạng thẻ thành công vào hộp thư " + request.recipientEmail() + " qua hệ thống SMTP Gmail!"
                        : "Hệ thống đã tạo bản tin mẫu. Nếu Gmail SMTP chưa kết nối, bản tin được lưu vào nhật ký."
        );

        return ResponseEntity.ok(ApiResponse.ok(responseData));
    }

    @PostMapping("/send-channel-digest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendChannelDigest(
            @Valid @RequestBody SendChannelDigestRequest request
    ) {
        String channel = (request.channelId() != null) ? request.channelId().toLowerCase().trim() : "shopping";
        List<EmailCenterService.DigestArticleCard> articles = new ArrayList<>();
        String emailSubject;
        String headerTitle;

        if ("shopping".equals(channel) || "shopee".equals(channel)) {
            emailSubject = "[AIFP Săn Deal] Cơ hội mua sắm Shopee giá hời hôm nay";
            headerTitle = "Bản tin Deal Shopee & Giá tốt chọn lọc";
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Shopee Mall",
                    "shop",
                    "Bàn phím cơ Keychron K2 Pro không dây giảm giá còn 1.890.000 đ",
                    "Đã đạt mức giá mục tiêu bạn theo dõi (tiết kiệm 160.000 đ so với giá thị trường), hỗ trợ bảo hành chính hãng 12 tháng và miễn phí vận chuyển toàn quốc.",
                    "https://shopee.vn/search?keyword=keychron+k2+pro",
                    "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=1200&auto=format&fit=crop&q=80",
                    "11:30 Trưa nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Shopee Mall",
                    "shop",
                    "Màn hình LG 27UP850N 4K UltraFine IPS giảm 18% còn 6.890.000 đ",
                    "Phiên bản chuẩn đồ họa DisplayHDR 400, tích hợp sạc Type-C 90W thích hợp cho MacBook và laptop đồ họa, số lượng ưu đãi có hạn.",
                    "https://shopee.vn/search?keyword=lg+27up850n",
                    "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=1200&auto=format&fit=crop&q=80",
                    "11:15 Trưa nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Shopee Mall",
                    "shop",
                    "Tai nghe chống ồn Sony WH-1000XM5 chính hãng flash sale còn 6.450.000 đ",
                    "Dòng tai nghe chống ồn cao cấp thế hệ 5 của Sony, hỗ trợ codec LDAC chuẩn âm thanh Hi-Res, thời lượng pin 30 giờ liên tục.",
                    "https://shopee.vn/search?keyword=sony+wh-1000xm5",
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1200&auto=format&fit=crop&q=80",
                    "11:00 Trưa nay"
            ));
        } else if ("jobs".equals(channel) || "career".equals(channel)) {
            emailSubject = "[AIFP Việc Làm] Cơ hội tuyển dụng IT & Công nghệ mới nhất";
            headerTitle = "Bản tin Tuyển dụng IT & Cơ hội việc làm";
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Tuyển dụng ITViec",
                    "jobs",
                    "Tuyển dụng Kỹ sư Lập trình Flutter & Mobile App (iOS / Android)",
                    "Mức lương 25 - 45 triệu. Doanh nghiệp công nghệ tuyển dụng chuyên gia Flutter, yêu cầu vững State Management (Riverpod/Bloc), kết nối Clean Architecture.",
                    "https://itviec.com/it-jobs/flutter",
                    "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=1200&auto=format&fit=crop&q=80",
                    "08:00 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Tuyển dụng ITViec",
                    "jobs",
                    "Senior Spring Boot & Java Cloud Backend Architect",
                    "Mức lương 35 - 60 triệu. Xây dựng Modular Monolith, tích hợp pgvector, tối ưu truy vấn phân tán và kết nối LLM AI Orchestrator.",
                    "https://itviec.com/it-jobs/java",
                    "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1200&auto=format&fit=crop&q=80",
                    "07:45 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "TopCV Tuyển dụng",
                    "jobs",
                    "AI / LLM Application Engineer (FastAPI & LangGraph) Remote",
                    "Mức lương $2,500 - $4,500. Thiết kế pipeline RAG, tích hợp công cụ kiểm chứng Citation Grounding và kiểm soát chi phí suy luận đa nhà cung cấp.",
                    "https://itviec.com/it-jobs/python",
                    "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=1200&auto=format&fit=crop&q=80",
                    "07:30 Sáng nay"
            ));
        } else if ("tech".equals(channel)) {
            emailSubject = "[AIFP Công Nghệ] Điểm tin Trí tuệ nhân tạo & Xu hướng số";
            headerTitle = "Bản tin Công nghệ & Trí tuệ nhân tạo";
            articles.add(new EmailCenterService.DigestArticleCard(
                    "VnExpress Số Hóa",
                    "tech",
                    "Công nghệ Bluetooth mới cho phép phát âm thanh lossless",
                    "Tổ chức Bluetooth SIG công bố chuẩn kết nối Bluetooth HDT với tốc độ tối đa 7,5 Mb/giây, hỗ trợ phát nhạc lossless chất lượng cao trực tiếp không nén.",
                    "https://vnexpress.net/cong-nghe-bluetooth-moi-cho-phep-phat-am-thanh-lossless-5141993.html",
                    "https://i1-vnexpress.vnecdn.net/2026/11/07/dsc8652-1794060485-2338-1794060541.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=eQVmaEeWfpBX2NhCWVOjkQ",
                    "07:30 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "VnExpress Công nghệ",
                    "tech",
                    "iPhone 20 có thể bỏ nút Action và giữ nguyên Dynamic Island",
                    "Apple lên kế hoạch gộp nút Action vào cụm điều khiển Camera Control, giữ Dynamic Island đồng thời tối ưu hóa không gian cho hệ thống pin thế hệ mới.",
                    "https://vnexpress.net/iphone-20-co-the-bo-nut-action-va-giu-nguyen-dynamic-island-5141990.html",
                    "https://i1-vnexpress.vnecdn.net/2026/11/07/fdnnyswsv5m3lw8bs4geuy-1794060-2723-9452-1794060076.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=AiQfW9d6L8Mn5AJ1XM6E9A",
                    "07:15 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "HackerNews AI",
                    "tech",
                    "Kiến trúc tự trị giảm thiểu rủi ro bảo mật cho trợ lý cá nhân",
                    "Phân tích giải pháp phân quyền công cụ 4 cấp độ và cách ly tuyệt đối nội dung bên ngoài để ngăn chặn tấn công Prompt Injection.",
                    "https://news.ycombinator.com/",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80",
                    "07:00 Sáng nay"
            ));
        } else if ("finance".equals(channel)) {
            emailSubject = "[AIFP Tài Chính] Nhịp đập thị trường & Kinh tế vĩ mô";
            headerTitle = "Bản tin Thị trường Tài chính & Đầu tư";
            articles.add(new EmailCenterService.DigestArticleCard(
                    "VnExpress Kinh Doanh",
                    "news",
                    "Nhà đầu tư ngờ vực mức định giá 2.000 tỷ USD của Anthropic",
                    "Sau phiên điều chỉnh cổ phiếu công nghệ tại Mỹ, giới đầu tư bắt đầu cân nhắc chi phí vận hành khổng lồ của các trung tâm dữ liệu AI so với doanh thu thực tế.",
                    "https://vnexpress.net/nha-dau-tu-ngo-vuc-muc-dinh-gia-2-000-ty-usd-cua-anthropic-5142087.html",
                    "https://i1-kinhdoanh.vnecdn.net/2026/11/08/afp-hl-jmbarrere-v1-highres-cl-9797-5057-1794104920.jpg?w=1200&h=0&q=100&dpr=1&fit=crop&s=7j1J3f3dWaJR8UMdREqVJQ",
                    "17:30 Chiều nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "CafeF Kinh tế",
                    "news",
                    "Thị trường chứng khoán phục hồi nhờ dòng tiền nội và nhóm cổ phiếu công nghệ",
                    "Chỉ số VN-Index tăng điểm tích cực với thanh khoản cải thiện, dòng tiền tập trung vào các doanh nghiệp đón đầu làn sóng số hóa.",
                    "https://cafef.vn/",
                    "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=1200&auto=format&fit=crop&q=80",
                    "17:15 Chiều nay"
            ));
        } else if ("ai-evolution".equals(channel) || "ai".equals(channel)) {
            emailSubject = "[AIFP Tiến Hóa AI] Báo cáo biến động AI & Lộ trình nâng cấp hệ thống hàng ngày";
            headerTitle = "Báo cáo Tiến hóa AI & Lộ trình Nâng cấp Hệ thống AIFP";
            articles.add(new EmailCenterService.DigestArticleCard(
                    "DeepSeek AI Research",
                    "tech",
                    "DeepSeek V3 / R1 và Cuộc cách mạng Reasoning mã nguồn mở",
                    "Kiến trúc Mixture-of-Experts (MoE) 671B với 37B active parameters chứng minh khả năng suy luận logic tương đương mô hình thương mại với chi phí inference giảm hơn 90%.",
                    "https://arxiv.org/abs/2412.19437",
                    "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=1200&auto=format&fit=crop&q=80",
                    "07:00 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Anthropic Engineering",
                    "tech",
                    "Giao thức Model Context Protocol (MCP) trở thành tiêu chuẩn mở toàn cầu",
                    "Tiêu chuẩn hóa giao tiếp JSON-RPC bảo mật giữa AI Agents và hệ thống dữ liệu doanh nghiệp (GitHub, Google Workspace, PostgreSQL, Slack), loại bỏ nhu cầu viết custom API wrapper.",
                    "https://modelcontextprotocol.io/",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80",
                    "06:45 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Google DeepMind & AI Blog",
                    "tech",
                    "Gemini 2.5 Flash / Pro và Cơ chế Context Caching tối ưu hóa ngân sách",
                    "Cơ chế lưu đệm ngữ cảnh giúp giảm 75% chi phí xử lý tài liệu lặp lại, mở đường cho trợ lý AI ghi nhớ vĩnh viễn dữ liệu người dùng mà không tăng chi phí suy luận theo thời gian.",
                    "https://deepmind.google/technologies/gemini/",
                    "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=1200&auto=format&fit=crop&q=80",
                    "06:30 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Hugging Face & Ollama",
                    "tech",
                    "Xu hướng On-Device SLM (Gemma 2B / Qwen 2.5): Xử lý cục bộ bảo mật 100%",
                    "Các mô hình ngôn ngữ nhỏ lượng tử hóa (4-bit quantization) chạy mượt mà trên chip di động và laptop cá nhân, cho phép phân loại tin tức và trích xuất thực thể ngoại tuyến hoàn toàn miễn phí.",
                    "https://huggingface.co/blog",
                    "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=1200&auto=format&fit=crop&q=80",
                    "06:00 Sáng nay"
            ));
        } else {
            emailSubject = "[AIFP Cộng Đồng] Thảo luận & Bài viết chuyên sâu nổi bật";
            headerTitle = "Bản tin Cộng đồng & Thảo luận Xã hội";
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Facebook Tech Group",
                    "fb",
                    "Thảo luận Giải pháp AI Agent tự hành trên di động: Cắt giảm 80% chi phí API",
                    "Bài viết phân tích kiến trúc kết hợp LangGraph trên server với Flutter Mobile, sử dụng mô hình nhỏ chạy offline và chuyển đổi thông minh đa nhà cung cấp.",
                    "https://www.facebook.com/groups/laptrinhflutter",
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80",
                    "20:00 Tối qua"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Cộng đồng Developer",
                    "fb",
                    "Kinh nghiệm xây dựng hệ thống tự động gửi tin tức không cần mở app",
                    "Chia sẻ cách thiết lập Cron Scheduler, Redis lock và tích hợp Gmail SMTP để trợ lý số vận hành hoàn toàn ngầm sau khi deploy.",
                    "https://www.facebook.com/groups/laptrinhvienvietnam",
                    "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=1200&auto=format&fit=crop&q=80",
                    "19:45 Tối qua"
            ));
        }

        String htmlBody;
        if ("ai-evolution".equals(channel) || "ai".equals(channel)) {
            String pastSummary = "AIFP đã xây dựng vững chắc 16/16 module từ Sprint 1 đến Phase 3, giải quyết triệt để bài toán cô lập dữ liệu theo user_id, phân quyền 4 cấp độ, mã hóa Web Push AES-256-GCM, khử trùng lặp nội dung SHA256 (DELTA_ONLY) và tự động hóa ngầm Zero-App-Open.";
            String presentSummary = "Nền tảng vận hành với Spring Boot 3 Modular Monolith kết hợp Python LangGraph 7-node, bộ định tuyến Failover xoay vòng đa nhà cung cấp (Gemini -> Groq -> OpenRouter -> OpenAI -> Ollama), và Gmail SMTP tự động cào quét tin tức CSDL.";
            String futureRoadmap = "Cần chủ động đón đầu 4 đột phá công nghệ: (1) Chuẩn hóa MCP Server mở rộng kết nối; (2) Tích hợp On-Device SLM (Gemma 2B / Qwen 2.5) nhúng cục bộ để phân loại tin tức miễn phí 100%; (3) Trợ lý đàm thoại giọng nói 2 chiều (Voice Duplex Streaming); (4) Vector Memory Compaction tự động tối ưu hóa bộ nhớ dài hạn.";
            List<String> suggestedImprovements = List.of(
                    "[Backend Spring Boot] Xây dựng McpBridgeClient.java hỗ trợ giao thức Model Context Protocol (JSON-RPC) để kết nối AIFP với kho công cụ MCP toàn cầu (GitHub, Notion, Figma, Slack).",
                    "[Python AI Service] Tích hợp engine cào quét tự động arXiv CS.AI và Hugging Face Daily Papers vào luồng cào quét ngầm, tự động chấm điểm độ liên quan đến kiến trúc AIFP.",
                    "[Frontend Web & Mobile] Xây dựng Bảng điều khiển 'Chỉ số Tiến hóa AI' (AI Evolution Index) hiển thị trực quan các đề xuất nâng cấp được AI đề xuất và cho phép duyệt áp dụng 1-click.",
                    "[Tối ưu Hiệu năng & Chi phí] Triển khai Context Caching cho các tác vụ tổng hợp tin tức lặp lại, giúp giảm thêm 60-75% chi phí gọi LLM hàng ngày."
            );
            htmlBody = emailCenterService.renderAiEvolutionHtml(
                    headerTitle,
                    "Hôm nay, 08/11/2026",
                    articles,
                    pastSummary,
                    presentSummary,
                    futureRoadmap,
                    suggestedImprovements
            );
        } else {
            htmlBody = emailCenterService.renderCardBasedDigestHtml(headerTitle, articles);
        }
        boolean delivered = emailCenterService.sendRealHtmlEmail(request.recipientEmail(), emailSubject, htmlBody);

        Map<String, Object> responseData = Map.of(
                "delivered", delivered,
                "recipientEmail", request.recipientEmail(),
                "channelId", channel,
                "channelTitle", headerTitle,
                "articlesCount", articles.size(),
                "fromSystemEmail", "nguyentuananh11a1.lhpt@gmail.com",
                "message", delivered
                        ? "Đã gửi bản tin riêng [" + headerTitle + "] thành công vào hộp thư " + request.recipientEmail() + " qua hệ thống SMTP Gmail!"
                        : "Bản tin đã được tạo mẫu thành công."
        );

        return ResponseEntity.ok(ApiResponse.ok(responseData));
    }
}

