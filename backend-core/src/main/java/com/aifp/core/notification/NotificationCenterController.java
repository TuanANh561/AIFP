package com.aifp.core.notification;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationCenterController {

    private final NotificationCenterService notificationService;
    private final EmailCenterService emailCenterService;
    private final LiveArticleRepository liveArticleRepository;
    private final LinkValidationService linkValidationService;
    private final String ownerEmail;

    public NotificationCenterController(
            NotificationCenterService notificationService,
            EmailCenterService emailCenterService,
            LiveArticleRepository liveArticleRepository,
            LinkValidationService linkValidationService,
            @Value("${aifp.owner-email:nguyentuananh11a1.lhpt@gmail.com}") String ownerEmail
    ) {
        this.notificationService = notificationService;
        this.emailCenterService = emailCenterService;
        this.liveArticleRepository = liveArticleRepository;
        this.linkValidationService = linkValidationService;
        this.ownerEmail = (ownerEmail != null && !ownerEmail.isBlank()) ? ownerEmail.trim() : "nguyentuananh11a1.lhpt@gmail.com";
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
                    "",
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
                    "",
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
        List<EmailCenterService.DigestArticleCard> verifiedArticles = linkValidationService.sanitizeArticles(sampleArticles);
        String htmlBody = emailCenterService.renderCardBasedDigestHtml(title, verifiedArticles);

        boolean delivered = emailCenterService.sendRealHtmlEmail(
                request.recipientEmail(),
                "[AIFP] " + title,
                htmlBody
        );

        Map<String, Object> responseData = Map.of(
                "delivered", delivered,
                "recipientEmail", request.recipientEmail(),
                "articlesCount", sampleArticles.size(),
                "fromSystemEmail", "smtp.service@aifp.local",
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

        boolean isProprietaryChannel = "ai-evolution".equals(channel) || "ai".equals(channel)
                || "creator-ideas".equals(channel) || "monetization-ideas".equals(channel) || "creator".equals(channel);
        boolean isOwner = request.recipientEmail() != null && request.recipientEmail().trim().equalsIgnoreCase(ownerEmail.trim());

        if (isProprietaryChannel && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kênh nghiên cứu và ý tưởng phát triển thuộc quyền sở hữu riêng của Master Owner");
        }

        List<EmailCenterService.DigestArticleCard> articles = new ArrayList<>();
        List<EmailCenterService.JobCardItem> jobCards = new ArrayList<>();
        List<EmailCenterService.MonetizationIdeaItem> monetizationIdeas = new ArrayList<>();
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
                    "",
                    "11:30 Trưa nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Shopee Mall",
                    "shop",
                    "Màn hình LG 27UP850N 4K UltraFine IPS giảm 18% còn 6.890.000 đ",
                    "Phiên bản chuẩn đồ họa DisplayHDR 400, tích hợp sạc Type-C 90W thích hợp cho MacBook và laptop đồ họa, số lượng ưu đãi có hạn.",
                    "https://shopee.vn/search?keyword=lg+27up850n",
                    "",
                    "11:15 Trưa nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Shopee Mall",
                    "shop",
                    "Tai nghe chống ồn Sony WH-1000XM5 chính hãng flash sale còn 6.450.000 đ",
                    "Dòng tai nghe chống ồn cao cấp thế hệ 5 của Sony, hỗ trợ codec LDAC chuẩn âm thanh Hi-Res, thời lượng pin 30 giờ liên tục.",
                    "https://shopee.vn/search?keyword=sony+wh-1000xm5",
                    "",
                    "11:00 Trưa nay"
            ));
        } else if ("jobs".equals(channel) || "career".equals(channel)) {
            emailSubject = "[AIFP Việc Làm] Cơ hội Việc làm Online: Test Game & Remote không yêu cầu kinh nghiệm";
            headerTitle = "Bản tin Cơ hội Việc làm & Remote Tuyển chọn";

            jobCards.add(new EmailCenterService.JobCardItem(
                    "Viec Lam Hub",
                    "Được tài trợ",
                    "CƠ HỘI VIỆC LÀM ONLINE: TESTER & TRẢI NGHIỆM GAME MOBILE",
                    "Cơ hội linh hoạt",
                    "Việt Nam",
                    "Part-time / Full-time",
                    "Thu nhập tham khảo: 1.100.000–5.800.000 VNĐ/ngày",
                    List.of(
                            "Hỗ trợ quảng bá thương hiệu, chơi thử và báo cáo lỗi game mobile",
                            "Có hướng dẫn chi tiết cho người mới bắt đầu từ A-Z",
                            "Quy trình đơn giản, không yêu cầu kinh nghiệm hay tiếng Anh"
                    ),
                    "Phù hợp cho ứng viên đang tìm cơ hội online linh hoạt và muốn tìm hiểu rõ hơn trước khi đăng ký.",
                    "* Tùy thuộc vào mức độ tham gia và hiệu quả công việc.",
                    "https://itviec.com/it-jobs/tester",
                    ""
            ));

            jobCards.add(new EmailCenterService.JobCardItem(
                    "Game Studio QA",
                    "Đã xác thực",
                    "TUYỂN CỘNG TÁC VIÊN CÀY GAME & REVIEW TỰA GAME MỚI",
                    "Làm việc tại nhà · Linh hoạt",
                    "Việt Nam · Remote",
                    "Part-time linh hoạt (2–4 giờ/ngày)",
                    "Thu nhập tham khảo: 800.000–3.200.000 VNĐ/tuần",
                    List.of(
                            "Chơi thử các bản game mobile mới phát hành và báo cáo lỗi phát sinh",
                            "Không yêu cầu kinh nghiệm, hướng dẫn từng bước qua tài liệu tiếng Việt",
                            "Không yêu cầu tiếng Anh, chỉ cần smartphone hoặc PC kết nối internet",
                            "Nhận thù lao theo tuần qua tài khoản ngân hàng hoặc ví MoMo"
                    ),
                    "Dành cho các bạn trẻ đam mê game, có thời gian rảnh rỗi muốn vừa giải trí vừa có thêm thu nhập.",
                    "* Thù lao thanh toán theo tuần dựa trên số lượng nhiệm vụ kiểm thử hoàn thành.",
                    "https://vieclam24h.vn/tim-kiem-viec-lam-nhanh?tu_khoa=game",
                    ""
            ));

            jobCards.add(new EmailCenterService.JobCardItem(
                    "TopCV Remote Hub",
                    "Đã kiểm duyệt",
                    "NHÂN VIÊN NHẬP LIỆU & HỖ TRỢ KHÁCH HÀNG ONLINE",
                    "Remote 100% không yêu cầu kinh nghiệm",
                    "Toàn quốc · Làm tại nhà",
                    "Part-time / Full-time",
                    "Thu nhập tham khảo: 1.100.000–4.500.000 VNĐ/tuần",
                    List.of(
                            "Nhập liệu danh mục sản phẩm và phản hồi tin nhắn khách hàng theo kịch bản",
                            "Không yêu cầu kinh nghiệm, đào tạo online 1 buổi là làm việc được ngay",
                            "Không yêu cầu tiếng Anh, làm việc 100% bằng tiếng Việt",
                            "Được chủ động đăng ký ca làm việc sáng, chiều hoặc tối linh hoạt"
                    ),
                    "Phù hợp cho ứng viên cần việc làm thêm ổn định tại nhà không gò bó thời gian.",
                    "* Mức thu nhập lũy tiến theo khối lượng dữ liệu xử lý thực tế.",
                    "https://vieclam24h.vn/tim-kiem-viec-lam-nhanh?tu_khoa=nhap+lieu",
                    ""
            ));

            jobCards.add(new EmailCenterService.JobCardItem(
                    "ITViec Hub",
                    "Cơ hội thực tập",
                    "THỰC TẬP SINH LẬP TRÌNH & JUNIOR DEV (INTERN / HYBRID)",
                    "Có phụ cấp thực tập & đào tạo bài bản",
                    "Hà Nội / TP.HCM · Hybrid",
                    "Full-time / Part-time",
                    "Phụ cấp: 5.000.000–12.000.000 VNĐ/tháng",
                    List.of(
                            "Tham gia phát triển dự án Flutter Mobile hoặc Java / Spring Boot Backend",
                            "Được các Senior Engineer hướng dẫn trực tiếp kiến trúc dự án thực tế",
                            "Cơ hội xem xét hợp đồng chính thức sau thời gian thực tập"
                    ),
                    "Dành cho sinh viên CNTT hoặc người mới học lập trình muốn trải nghiệm môi trường thực tế.",
                    "* Yêu cầu tinh thần chủ động học hỏi và tư duy logic cơ bản.",
                    "https://itviec.com/it-jobs/intern",
                    ""
            ));

            // Rigorously sanitize job links to guarantee 100% live status (no 404 or 403)
            jobCards = linkValidationService.sanitizeJobs(jobCards);

            // Persist jobs to live_articles in PostgreSQL
            for (EmailCenterService.JobCardItem jc : jobCards) {
                if (!liveArticleRepository.existsByUrl(jc.applyUrl())) {
                    String bulletsCombined = String.join("\n• ", jc.bulletPoints());
                    String summary = jc.subtitle() + " | " + jc.salaryRange() + "\n• " + bulletsCombined + "\n" + jc.note();
                    liveArticleRepository.save(new LiveArticleEntity(
                            UUID.randomUUID(),
                            "jobs",
                            jc.sourceName(),
                            "tech",
                            "Tuyển dụng Việc làm",
                            jc.title(),
                            summary,
                            "",
                            jc.applyUrl(),
                            Instant.now()
                    ));
                }
            }

            // Also keep standard article list for backwards compatibility
            for (EmailCenterService.JobCardItem jc : jobCards) {
                articles.add(new EmailCenterService.DigestArticleCard(
                        jc.sourceName(),
                        "jobs",
                        jc.title(),
                        jc.salaryRange() + " — " + jc.description(),
                        jc.applyUrl(),
                        "",
                        "08:00 Sáng nay"
                ));
            }
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
                    "",
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
                    "",
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
                    "",
                    "07:00 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Anthropic Engineering",
                    "tech",
                    "Giao thức Model Context Protocol (MCP) trở thành tiêu chuẩn mở toàn cầu",
                    "Tiêu chuẩn hóa giao tiếp JSON-RPC bảo mật giữa AI Agents và hệ thống dữ liệu doanh nghiệp (GitHub, Google Workspace, PostgreSQL, Slack), loại bỏ nhu cầu viết custom API wrapper.",
                    "https://modelcontextprotocol.io/",
                    "",
                    "06:45 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Google DeepMind & AI Blog",
                    "tech",
                    "Gemini 2.5 Flash / Pro và Cơ chế Context Caching tối ưu hóa ngân sách",
                    "Cơ chế lưu đệm ngữ cảnh giúp giảm 75% chi phí xử lý tài liệu lặp lại, mở đường cho trợ lý AI ghi nhớ vĩnh viễn dữ liệu người dùng mà không tăng chi phí suy luận theo thời gian.",
                    "https://deepmind.google/technologies/gemini/",
                    "",
                    "06:30 Sáng nay"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Hugging Face & Ollama",
                    "tech",
                    "Xu hướng On-Device SLM (Gemma 2B / Qwen 2.5): Xử lý cục bộ bảo mật 100%",
                    "Các mô hình ngôn ngữ nhỏ lượng tử hóa (4-bit quantization) chạy mượt mà trên chip di động và laptop cá nhân, cho phép phân loại tin tức và trích xuất thực thể ngoại tuyến hoàn toàn miễn phí.",
                    "https://huggingface.co/blog",
                    "",
                    "06:00 Sáng nay"
            ));
        } else if ("creator-ideas".equals(channel) || "monetization-ideas".equals(channel) || "creator".equals(channel)) {
            emailSubject = "[Aura Ý Tưởng] Chiến lược Kiếm tiền, Làm Game & Sáng tạo Video Đa nền tảng";
            headerTitle = "Chiến Lược Kiếm Tiền & Hiện Thực Hóa Ý Tưởng Sáng Tạo";

            monetizationIdeas.add(new EmailCenterService.MonetizationIdeaItem(
                    "HYBRID_GAMING_VIDEO",
                    "Kênh YouTube Gameplay Thử Thách & Lỗi Game Hài Hước (Đòn bẩy AI)",
                    "YouTube Dài (8–12p) + TikTok/Shorts (30s)",
                    "$600 – $2,500 / tháng (AdSense + Tài trợ)",
                    "Khán giả xem video game là vì 'con người thật' (phản xạ giật mình, pha xử lý lỗi hài hước, cảm xúc thật). Video 100% bằng AI dùng giọng đọc đều đều và ảnh ghép khiến người xem vuốt đi ngay sau 1.5 giây, kéo tụt retention. Thay vào đó, hãy lấy chính footage bạn chơi game làm TRỌNG TÂM, và dùng AI để: lên ý tưởng thử thách lạ, viết hook 3 giây đầu, viết mô tả SEO và thiết kế thumbnail bắt mắt.",
                    List.of(
                            "Bước 1 - Lên ý tưởng thử thách độc đáo bằng AI: Dùng prompt: 'Gợi ý 5 thử thách oái oăm trong tựa game X khiến người xem tò mò ngay từ 3 giây đầu tiên'.",
                            "Bước 2 - Bật OBS ghi hình 45–60 phút bạn tự chơi: Đừng diễn, hãy giữ micro thu lại mọi cảm xúc tự nhiên, tiếng cười, tiếng thất vọng thật nhất.",
                            "Bước 3 - Nhận diện điểm cao trào (The Story Arc): Chọn ra 1 pha thất bại thảm hại ở đầu video và 1 pha vượt qua nghẹt thở ở cuối video.",
                            "Bước 4 - Dựng nhịp nhanh bằng CapCut / Premiere: Cắt bỏ các đoạn đi bộ im lặng, chèn meme sound effect và phụ đề động tại những câu nói đắt giá.",
                            "Bước 5 - Đăng đa nền tảng tối ưu phễu người xem: Đăng video dài đầy đủ lên YouTube. Cắt đoạn hài hước nhất (30s) đăng lên TikTok và YouTube Shorts kèm lời kêu gọi 'Xem pha xử lý full tại link trên bio'."
                    ),
                    "OBS Studio (Quay hình) · CapCut Pro (Dựng nhanh) · Claude / Gemini (Soạn Hook & Title) · Canva / Photoshop (Thumbnail)",
                    "Mở OBS lên, vào tựa game bạn thích nhất, chơi 1 trận với mục tiêu làm một điều kỳ quặc và ghi lại 15 phút video thô."
            ));

            monetizationIdeas.add(new EmailCenterService.MonetizationIdeaItem(
                    "SOLO_INDIE_GAME",
                    "Phát Triển Game Độc Lập Mini Bằng Godot 4 & AI (Solo Indie Game)",
                    "Steam ($2.99–$4.99) · Itch.io · Roblox",
                    "$1,000 – $5,000 / dự án hoàn thiện",
                    "Rất nhiều người mơ làm game nhưng bỏ cuộc vì code khó và thiếu đồ họa. Với AI hiện tại, bạn không cần studio lớn. Bạn chỉ cần tư duy Gameplay Loop thật cuốn hút, còn AI sẽ viết code GDScript/C# cho từng hàm di chuyển, sinh quái, tính điểm và tạo cốt truyện nhiệm vụ.",
                    List.of(
                            "Bước 1 - Chọn thể loại game nhỏ gọn đã chứng minh thành công: Vampire Survivors-like (sinh tồn 2D), Roguelike thẻ bài hoặc Escape Room giải đố.",
                            "Bước 2 - Dựng bản chơi thử (Prototype) trong 48 giờ: Tải Godot Engine 4 (nhẹ 50MB). Nhờ AI viết script GDScript cho Player Movement và Enemy Spawner.",
                            "Bước 3 - Lấy Asset đồ họa bản quyền miễn phí: Sử dụng kho Kenney.nl hoặc OpenGameArt (đầy đủ sprite 2D, tilemap, sound effect 100% miễn phí thương mại).",
                            "Bước 4 - Đưa cơ chế tạo nhiệm vụ / lời thoại bằng AI: Dùng AI viết 20 câu thoại trêu chọc người chơi khi chết để tạo nét hài hước độc bản.",
                            "Bước 5 - Đăng bản Demo lên Itch.io & Steam: Mở trang Steam Wishlist sớm trước khi game ra mắt để gom người theo dõi.",
                            "Bước 6 - Dùng chính video làm game (Devlog) để bán game: Quay lại những khoảnh khắc fix bug dở khóc dở cười đăng lên TikTok. Đây là cách các solo dev thu hút hàng trăm nghìn lượt tải mà không tốn tiền quảng cáo!"
                    ),
                    "Godot 4.3 (Game Engine miễn phí) · Kenney.nl (Kho Asset 2D) · Claude 3.5 Sonnet (Viết code logic GDScript) · Audacity (Xử lý âm thanh)",
                    "Tải Godot Engine 4 về máy, copy đoạn code di chuyển 8 hướng do AI tạo và bấm Play để nhìn thấy nhân vật đầu tiên di chuyển trên màn hình!"
            ));

            monetizationIdeas.add(new EmailCenterService.MonetizationIdeaItem(
                    "AI_CREATIVE_SERVICES",
                    "Dịch Vụ Thiết Kế Kịch Bản & Cốt Truyện Game Cho Khách Hàng Nước Ngoài",
                    "Upwork · Fiverr · Ybox · Diễn đàn Game Dev",
                    "$50 – $300 / kịch bản cốt truyện & chuỗi nhiệm vụ",
                    "Các lập trình viên game thường rất giỏi kỹ thuật nhưng lại rất yếu về khoản viết văn, xây dựng thế giới (World-building) và hội thoại nhân vật. Bạn có thể đóng vai trò 'Narrative Designer' sử dụng AI để tạo ra các kịch bản chi tiết, phân nhánh lựa chọn và lời thoại NPC chuẩn văn phong bản ngữ.",
                    List.of(
                            "Bước 1 - Tạo Profile chuyên về Narrative Design / Scriptwriting trên Fiverr và Upwork.",
                            "Bước 2 - Xây dựng Portfolio mẫu gồm 3 cốt truyện game: 1 game kinh dị sinh tồn, 1 game nhập vai fantasy, 1 kịch bản video YouTube triệu view.",
                            "Bước 3 - Nhận yêu cầu từ khách và dùng AI tạo khung xương (Story Arc): Thiết lập tiểu sử nhân vật, động lực, biến cố và các nhánh kết thúc.",
                            "Bước 4 - Tinh chỉnh bằng trải nghiệm game thực tế của bạn: Thêm các câu đùa, thuật ngữ game thực tế để kịch bản không bị cảm giác máy móc.",
                            "Bước 5 - Đóng gói file PDF chuyên nghiệp và bàn giao cho khách kèm gợi ý góc quay hoặc nhạc nền."
                    ),
                    "Gemini 2.5 Flash · Notion (Lưu trữ kịch bản) · Twine (Thiết kế hội thoại phân nhánh game)",
                    "Viết thử 1 kịch bản cốt truyện ngắn 500 từ về một tựa game nhập vai giả tưởng và lưu vào kho portfolio của bạn."
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
                    "",
                    "20:00 Tối qua"
            ));
            articles.add(new EmailCenterService.DigestArticleCard(
                    "Cộng đồng Developer",
                    "fb",
                    "Kinh nghiệm xây dựng hệ thống tự động gửi tin tức không cần mở app",
                    "Chia sẻ cách thiết lập Cron Scheduler, Redis lock và tích hợp Gmail SMTP để trợ lý số vận hành hoàn toàn ngầm sau khi deploy.",
                    "https://www.facebook.com/groups/laptrinhvienvietnam",
                    "",
                    "19:45 Tối qua"
            ));
        }

        // Rigorously sanitize all news article links before email dispatch
        if (!articles.isEmpty()) {
            articles = linkValidationService.sanitizeArticles(articles);
        }

        String htmlBody;
        if ("jobs".equals(channel) || "career".equals(channel)) {
            htmlBody = emailCenterService.renderJobCardDigestHtml(headerTitle, request.recipientEmail(), jobCards);
        } else if ("ai-evolution".equals(channel) || "ai".equals(channel)) {
            String pastSummary = "AIFP đã xây dựng vững chắc 16/16 module từ Sprint 1 đến Phase 3, giải quyết triệt để bài toán cô lập dữ liệu theo user_id, phân quyền 4 cấp độ, mã hóa Web Push AES-256-GCM, khử trùng lặp nội dung SHA256 (DELTA_ONLY) và tự động hóa ngầm Zero-App-Open.";
            String presentSummary = "Nền tảng vận hành với Spring Boot 3 Modular Monolith kết hợp Python LangGraph 7-node, bộ định tuyến Failover xoay vòng đa nhà cung cấp (Gemini -> Groq -> OpenRouter -> OpenAI -> Ollama), và Gmail SMTP tự động cào quét tin tức CSDL.";
            String futureRoadmap = "Cần chủ động đón đầu 4 đột phá công nghệ: (1) Chuẩn hóa MCP Server mở rộng kết nối; (2) Tích hợp On-Device SLM (Gemma 2B / Qwen 2.5) nhúng cục bộ để phân loại tin tức miễn phí 100%; (3) Trợ lý đàm thoại giọng nói 2 chiều (Voice Duplex Streaming); (4) Vector Memory Compaction tự động tối ưu hóa bộ nhớ dài hạn.";
            List<String> suggestedImprovements = List.of(
                    "[Backend Spring Boot] Xây dựng McpBridgeClient.java hỗ trợ giao thức Model Context Protocol (JSON-RPC) để kết nối AIFP với kho công cụ MCP toàn cầu (GitHub, Notion, Figma, Slack).",
                    "[Python AI Service] Tích hợp engine cào quét tự động arXiv CS.AI và Hugging Face Daily Papers vào luồng cào quét ngầm, tự động chấm điểm độ liên quan đến kiến trúc AIFP.",
                    "[Frontend Web & Mobile] Xây dựng Bảng điều khiển 'Chỉ số Tiến hóa AI' (AI Evolution Index) hiển thị trực quan các đề xuất nâng cấp được AI đề xuất và cho phép duyệt áp dụng 1-click.",
                    "[Tối ưu Hiệu năng & Chi phí] Triển khai Context Caching cho các tác vụ tổng hợp tin tức lặp lại, giúp giảm thêm 60-75% chi phí gọi LLM hàng ngày."
            );

            List<EmailCenterService.OpenSourceProposalItem> openSourceProposals = List.of(
                    new EmailCenterService.OpenSourceProposalItem(
                            "unclecode/crawl4ai",
                            "https://github.com/unclecode/crawl4ai",
                            "★ 34.5k",
                            "Apache-2.0",
                            "Async LLM-Friendly Web & Dynamic Crawler",
                            "Tăng tốc cào quét tin tức & việc làm lên gấp 5 lần, bypass các trang chặn bot, trích xuất Markdown sạch không lẫn quảng cáo hay boilerplate.",
                            "Tích hợp vào ai-service/app/crawlers/, nâng cấp bộ bóc tách tin tức, giảm 80% thời gian xử lý HTML so với phương pháp truyền thống.",
                            "from crawl4ai import AsyncWebCrawler\nasync with AsyncWebCrawler() as crawler:\n    res = await crawler.arun(url='https://example.com')\n    clean_markdown = res.markdown"
                    ),
                    new EmailCenterService.OpenSourceProposalItem(
                            "modelcontextprotocol/python-sdk",
                            "https://github.com/modelcontextprotocol",
                            "★ 18.2k",
                            "MIT",
                            "Model Context Protocol (MCP) Standard",
                            "Chuẩn hóa 4-Level Tool Gate sang giao thức MCP mở toàn cầu, cho phép AIFP kết nối sẵn với hơn 1,000 công cụ MCP (GitHub, Notion, SQLite, Brave Search, Google Drive).",
                            "Xây dựng McpBridgeClient trong Java Spring Boot & Python LangGraph, mở rộng vô hạn khả năng tích hợp mà không phải code custom API.",
                            "{\n  \"jsonrpc\": \"2.0\",\n  \"method\": \"tools/call\",\n  \"params\": {\"name\": \"query_database\", \"arguments\": {\"sql\": \"...\"}}\n}"
                    ),
                    new EmailCenterService.OpenSourceProposalItem(
                            "mem0ai/mem0",
                            "https://github.com/mem0ai/mem0",
                            "★ 26.8k",
                            "Apache-2.0",
                            "The Memory Layer for AI Assistants",
                            "Tự động học hỏi thói quen, lịch trình, sở thích của người dùng qua các phiên chat, tự động giải quyết xung đột thông tin (conflict resolution) và nén bộ nhớ.",
                            "Đồng bộ hai chiều với bảng personal_memories trong PostgreSQL + pgvector, giúp trợ lý ngày càng thông minh và cá nhân hóa theo từng ngày.",
                            "from mem0 import Memory\nm = Memory()\nm.add('Người dùng muốn nhận email việc làm Game Tester lúc 08:00', user_id=user_id)"
                    ),
                    new EmailCenterService.OpenSourceProposalItem(
                            "ollama/ollama",
                            "https://github.com/ollama/ollama",
                            "★ 118k",
                            "MIT",
                            "Local SLM Engine (Gemma 2 / Qwen 2.5)",
                            "Chạy mô hình ngôn ngữ nhỏ hoàn toàn offline trên máy tính hoặc server cục bộ, phục vụ phân loại tin tức và kiểm tra trùng lặp DELTA_ONLY với chi phí 0 VNĐ.",
                            "Bảo mật tuyệt đối thông tin nhạy cảm (Zero Data Leakage), hoạt động bền bỉ ngay cả khi mất kết nối Internet hoặc hết quota API đám mây.",
                            "ollama run qwen2.5:3b \"Phân loại bài viết theo chủ đề và trích xuất thực thể chính\""
                    ),
                    new EmailCenterService.OpenSourceProposalItem(
                            "livekit/agents",
                            "https://github.com/livekit/agents",
                            "★ 8.5k",
                            "Apache-2.0",
                            "Real-Time Voice Duplex & Multimodal Bridge",
                            "Chuyển hóa trợ lý từ gõ văn bản sang đàm thoại trực tiếp 2 chiều độ trễ siêu thấp (< 300ms) trên Web PWA và Mobile Flutter.",
                            "Kết nối WebRTC giữa Web/Mobile client và Python Worker, tích hợp Deepgram / Faster-Whisper STT và Cartesia / Piper TTS.",
                            "agent = VoicePipelineAgent(\n    vad=silero.VAD.load(),\n    stt=deepgram.STT(),\n    llm=google.LLM(),\n    tts=cartesia.TTS()\n)"
                    )
            );

            htmlBody = emailCenterService.renderAiEvolutionHtml(
                    headerTitle,
                    "Hôm nay, 08/11/2026",
                    articles,
                    openSourceProposals,
                    pastSummary,
                    presentSummary,
                    futureRoadmap,
                    suggestedImprovements,
                    "http://localhost:5173/"
            );
        } else if ("creator-ideas".equals(channel) || "monetization-ideas".equals(channel) || "creator".equals(channel)) {
            String algorithmInsight = "Khi bạn đăng video thuần AI (chữ chạy, giọng máy, hình stock), khán giả lập tức nhận ra đây là nội dung tự động sản xuất hàng loạt và lướt qua ngay. Thuật toán YouTube Shorts & TikTok trừng phạt nặng nề nội dung có tỷ lệ giữ chân (Retention) thấp dưới 60%. Ngược lại, video bạn tự quay khi chơi game mang cảm xúc con người thật (Human Soul) — tiếng cười, pha xử lý lỗi hay khoảnh khắc bất ngờ — đây chính là thứ giữ chân người xem. Công thức chiến thắng là BẠN làm nhân vật chính, và AI là trợ lý hậu cần (soạn kịch bản, làm thumbnail, tối ưu từ khóa).";
            htmlBody = emailCenterService.renderMonetizationIdeaDigestHtml(
                    headerTitle,
                    "Hôm nay, 08/11/2026",
                    request.recipientEmail(),
                    algorithmInsight,
                    monetizationIdeas,
                    "http://localhost:5173/"
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
                "fromSystemEmail", "smtp.service@aifp.local",
                "message", delivered
                        ? "Đã gửi bản tin riêng [" + headerTitle + "] thành công vào hộp thư " + request.recipientEmail() + " qua hệ thống SMTP Gmail!"
                        : "Bản tin đã được tạo mẫu thành công."
        );

        return ResponseEntity.ok(ApiResponse.ok(responseData));
    }
}

