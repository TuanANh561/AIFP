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

        String htmlBody = emailCenterService.renderCardBasedDigestHtml(headerTitle, articles);
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

