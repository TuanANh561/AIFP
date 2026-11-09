package com.aifp.core.chat;

import com.aifp.core.auth.UserEntity;
import com.aifp.core.auth.UserProfileEntity;
import com.aifp.core.auth.UserProfileRepository;
import com.aifp.core.auth.UserRepository;
import com.aifp.core.common.AiOrchestratorClient;
import com.aifp.core.task.AiTaskEntity;
import com.aifp.core.task.AiTaskRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AiTaskRepository taskRepository;
    private final AiOrchestratorClient aiOrchestratorClient;

    public ChatService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            AiTaskRepository taskRepository,
            AiOrchestratorClient aiOrchestratorClient
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.taskRepository = taskRepository;
        this.aiOrchestratorClient = aiOrchestratorClient;
    }

    public record CreateConversationRequest(@NotBlank @Size(max = 200) String title) {}

    public record SendMessageRequest(@NotBlank @Size(max = 16000) String content) {}

    public record ChatTurnResponse(
            UUID conversationId,
            MessageEntity userMessage,
            MessageEntity assistantMessage,
            String selectedModel,
            boolean quotaDowngraded
    ) {}

    @Transactional
    public ConversationEntity createConversation(UUID userId, String title) {
        ConversationEntity conv = new ConversationEntity(UUID.randomUUID(), userId, title.trim());
        return conversationRepository.save(conv);
    }

    @Transactional(readOnly = true)
    public List<ConversationEntity> listConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> getConversationMessages(UUID userId, UUID conversationId) {
        // Strict Tenant Isolation: verify conversation belongs to userId first
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        return messageRepository.findByConversationIdAndUserIdOrderByCreatedAtAsc(conversationId, userId);
    }

    @Transactional
    public ChatTurnResponse sendMessage(UUID userId, UUID conversationId, @Valid SendMessageRequest request) {
        ConversationEntity conv = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getDailyTokensUsed() >= user.getDailyTokenQuota()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily AI token quota exceeded");
        }

        boolean quotaWarning = user.getDailyTokensUsed() >= (user.getDailyTokenQuota() * 0.8);
        String rawContent = request.content().trim();
        String lowerQuery = rawContent.toLowerCase();

        String selectedModel = "gemini-2.5-flash";
        String replyContent;
        String citationsJsonString = "[]";

        // 1. Social & Casual Greetings (alo, chào bạn, hi, hello, bạn là ai, etc.)
        if (isGreetingOrCasual(lowerQuery)) {
            replyContent = "Chào bạn! Tôi là Trợ lý AI của hệ thống AIFP — nền tảng tự động hóa cuộc sống và làm việc thông minh.\n\n" +
                    "Tôi có thể hỗ trợ bạn trực tiếp các chức năng trong toàn hệ thống:\n" +
                    "• 🎬 **Tạo Video & Shorts:** Lên kịch bản AI, storyboard phân cảnh, tích hợp bộ công cụ mã nguồn mở (Stickman Video Director, Lemo-Opucas, MoviePy, Remotion).\n" +
                    "• 📰 **Bản tin & Tuyển dụng:** Cập nhật tin công nghệ, việc làm remote, cày cấp và test server game.\n" +
                    "• 🧾 **Quét hóa đơn (Vision OCR):** Bóc tách tự động tên cửa hàng, ngày lập, chi tiết món hàng, thuế VAT và tổng thanh toán.\n" +
                    "• 🛍️ **Săn Deal Shopee:** Theo dõi biến động giá thiết bị và đồ công nghệ chọn lọc.\n" +
                    "• ⚡ **Tự động hóa:** Thiết lập lịch gửi bản tin tổng hợp tự động vào email mỗi sáng (không cần mở ứng dụng hàng ngày).\n\n" +
                    "Hôm nay bạn cần tôi hỗ trợ việc gì?";
            replyContent += "\n\n<!-- NAV_ACTION: {\"actions\":[{\"title\":\"Tạo video\",\"tab\":\"tools\",\"subTab\":\"video\"},{\"title\":\"Xem việc làm\",\"tab\":\"digests\",\"filter\":\"jobs\"},{\"title\":\"Quét hóa đơn\",\"tab\":\"tools\",\"subTab\":\"vision\"},{\"title\":\"Săn deal Shopee\",\"tab\":\"digests\",\"filter\":\"shopping\"}]} -->";

        // 2. Video Studio & Shorts Assistant
        } else if (isVideoIntent(lowerQuery)) {
            replyContent = "Hệ thống AIFP tích hợp sẵn **AI Video Studio & Shorts Director** hỗ trợ bạn sáng tạo nội dung:\n\n" +
                    "• **Công thức Retention cao:** Tận dụng footage gameplay con người thật (cảm xúc, reaction) làm linh hồn, AI hỗ trợ hook 3 giây đầu, viết kịch bản phân cảnh và tối ưu SEO.\n" +
                    "• **Bộ công cụ mã nguồn mở:** Hỗ trợ kịch bản storyboard, tích hợp Lemo-Opucas, Stickman Video Director, Remotion React Video và MoviePy/FFmpeg pipeline.\n" +
                    "• **Thao tác nhanh:** Bạn có thể bấm nút bên dưới để mở ngay màn hình Video Studio và tạo kịch bản/video ngay.";
            replyContent += "\n\n<!-- NAV_ACTION: {\"actions\":[{\"title\":\"Mở Video Studio\",\"tab\":\"tools\",\"subTab\":\"video\"},{\"title\":\"Xem ý tưởng kiếm tiền\",\"tab\":\"digests\",\"filter\":\"creator-ideas\"}]} -->";

        // 3. Receipt Scanning & Vision OCR
        } else if (isVisionIntent(lowerQuery)) {
            replyContent = "Hệ thống AIFP trang bị công cụ **Quét Hóa Đơn & Nhận Diện Thị Giác (Vision OCR)**:\n\n" +
                    "• Hỗ trợ tải ảnh chụp hóa đơn VAT, biên lai siêu thị, quán cà phê, hóa đơn điện tử.\n" +
                    "• Tự động trích xuất: Đơn vị phát hành, Ngày lập, Danh sách từng món, Thuế VAT và Tổng tiền.\n" +
                    "• Phân loại danh mục chi tiêu tự động và lưu trữ vào sổ quỹ cá nhân an toàn.";
            replyContent += "\n\n<!-- NAV_ACTION: {\"actions\":[{\"title\":\"Mở Quét Hóa Đơn\",\"tab\":\"tools\",\"subTab\":\"vision\"}]} -->";

        // 4. Jobs & Recruitment Search
        } else if (isJobsIntent(lowerQuery) && !isExplicitAutomationSchedule(lowerQuery)) {
            replyContent = "Các cơ hội việc làm chọn lọc đang mở tuyển dụng trong hệ thống AIFP:\n\n" +
                    "1. **Chơi game kiểm thử / cày cấp tại nhà** · Part-time linh hoạt (Thu nhập: 800.000 – 3.200.000 đ/tuần, không yêu cầu kinh nghiệm).\n" +
                    "2. **Thực tập sinh Lập trình viên Flutter & Java Spring Boot** · Trợ cấp 6 – 10 triệu/tháng, đào tạo bài bản.\n" +
                    "3. **Cộng tác viên gán nhãn dữ liệu AI & Nhập liệu Remote** · Làm việc từ xa, không yêu cầu tiếng Anh.\n\n" +
                    "Bạn có thể mở danh sách đầy đủ để xem chi tiết và ứng tuyển trực tiếp.";
            replyContent += "\n\n<!-- NAV_ACTION: {\"actions\":[{\"title\":\"Xem Danh Sách Việc Làm\",\"tab\":\"digests\",\"filter\":\"jobs\"}]} -->";

        // 5. Shopping & Deals
        } else if (isShoppingIntent(lowerQuery) && !isExplicitAutomationSchedule(lowerQuery)) {
            replyContent = "Cập nhật giá và cơ hội mua sắm công nghệ hôm nay:\n\n" +
                    "• **Bàn phím cơ Keychron K2 Pro không dây:** 1.890.000 đ tại Shopee Mall (Giảm 160.000 đ, miễn phí vận chuyển).\n" +
                    "• **Màn hình đồ họa LG 27UP850N 4K UltraFine IPS:** 6.890.000 đ (Sạc Type-C 90W chuẩn DisplayHDR).\n\n" +
                    "Bạn có thể mở mục Bản tin Săn Deal để theo dõi thêm các sản phẩm khác.";
            replyContent += "\n\n<!-- NAV_ACTION: {\"actions\":[{\"title\":\"Xem Deal Shopee\",\"tab\":\"digests\",\"filter\":\"shopping\"},{\"title\":\"Công cụ Mua sắm\",\"tab\":\"tools\",\"subTab\":\"shopping\"}]} -->";

        // 6. Explicit Automation Setup (ONLY when user explicitly requests scheduled email sending)
        } else if (isExplicitAutomationSchedule(lowerQuery)) {
            String recipientEmail = user.getEmail();
            if (userProfileRepository != null) {
                Optional<UserProfileEntity> prof = userProfileRepository.findById(userId);
                if (prof.isPresent() && prof.get().getNotificationEmail() != null && !prof.get().getNotificationEmail().isBlank()) {
                    recipientEmail = prof.get().getNotificationEmail().trim();
                }
            }

            String channelId;
            String title;
            String scheduledTime = "08:00";
            String frequency = "Hàng ngày";
            List<String> keywords;

            if (lowerQuery.contains("kiếm tiền") || lowerQuery.contains("làm game") || lowerQuery.contains("video") || lowerQuery.contains("kịch bản") || lowerQuery.contains("youtube") || lowerQuery.contains("tiktok") || lowerQuery.contains("short")) {
                channelId = "creator-ideas";
                title = "Ý Tưởng Kiếm Tiền & Hiện Thực Hóa Sáng Tạo (Game, Video)";
                keywords = List.of("Game Indie Godot", "YouTube Gaming Hybrid", "Kịch bản AI", "TikTok Shorts", "Kiếm tiền Online");
                scheduledTime = "20:00";
            } else if (lowerQuery.contains("mã nguồn mở") || lowerQuery.contains("open source") || lowerQuery.contains("tự học") || lowerQuery.contains("tiến hóa")) {
                channelId = "ai-evolution";
                title = "Tự Học Hỏi Hàng Ngày & Đề Xuất Tự Phát Triển (Mã Nguồn Mở)";
                keywords = List.of("Crawl4AI", "Model Context Protocol", "Mem0", "Ollama SLM", "LiveKit");
                scheduledTime = "07:00";
            } else if (lowerQuery.contains("game") || lowerQuery.contains("việc") || lowerQuery.contains("job") || lowerQuery.contains("remote")) {
                channelId = "jobs";
                title = "Tuyển dụng Test Game & Remote Tuyển chọn";
                keywords = List.of("Test Game", "Cày Game", "Remote không kinh nghiệm", "Không tiếng Anh");
                scheduledTime = "08:00";
            } else if (lowerQuery.contains("shopee") || lowerQuery.contains("deal") || lowerQuery.contains("giá")) {
                channelId = "shopping";
                title = "Săn Deal & Khuyến mãi Shopee Chọn lọc";
                keywords = List.of("Keychron", "Màn hình 4K", "Flash Sale");
                scheduledTime = "11:30";
            } else {
                channelId = "tech";
                title = "Tin tức Công nghệ & AI Chọn lọc";
                keywords = List.of("Mô hình AI mới", "Công nghệ số", "Bảo mật");
                scheduledTime = "07:00";
            }

            // Extract time if specified
            if (lowerQuery.contains("7h") || lowerQuery.contains("07:00")) scheduledTime = "07:00";
            if (lowerQuery.contains("8h") || lowerQuery.contains("08:00")) scheduledTime = "08:00";
            if (lowerQuery.contains("11h") || lowerQuery.contains("11:30")) scheduledTime = "11:30";
            if (lowerQuery.contains("12h") || lowerQuery.contains("12:00")) scheduledTime = "12:00";
            if (lowerQuery.contains("20h") || lowerQuery.contains("20:00")) scheduledTime = "20:00";

            AiTaskEntity newTask = new AiTaskEntity(
                    UUID.randomUUID(),
                    userId,
                    title,
                    "Luồng tự động hóa cào quét và gửi email theo chủ đề [" + title + "] vào lúc " + scheduledTime + " (" + frequency + "). Lọc các tin tức/việc làm mới có liên kết trực tiếp và lưu trữ vào CSDL.",
                    "DELTA_ONLY"
            );
            taskRepository.save(newTask);

            replyContent = "Đã thiết lập luồng gửi email tự động cho bạn:\n\n" +
                    "• **Chủ đề:** " + title + "\n" +
                    "• **Lịch gửi:** " + scheduledTime + " (" + frequency + ")\n" +
                    "• **Email nhận:** " + recipientEmail + "\n" +
                    "• **Từ khóa:** " + String.join(", ", keywords) + "\n\n" +
                    "Hệ thống sẽ tự động tổng hợp và gửi mail cho bạn đúng khung giờ trên mà không cần mở ứng dụng hàng ngày.";

            String kwJson = "[\"" + String.join("\",\"", keywords) + "\"]";
            replyContent += "\n\n<!-- AUTOMATION_ACTION: {\"action\":\"SETUP_CHANNEL\",\"channelId\":\"" + channelId + "\",\"title\":\"" + title + "\",\"scheduledTime\":\"" + scheduledTime + "\",\"frequency\":\"" + frequency + "\",\"customEmail\":\"" + recipientEmail + "\",\"keywords\":" + kwJson + "} -->";

        // 7. General Assistant Inquiry & Knowledge QA
        } else {
            AiOrchestratorClient.AgentExecutionResult execResult = aiOrchestratorClient.executeAgentTask(
                    userId,
                    rawContent,
                    List.of("searxng_search", "webpage_extract", "rss_fetch"),
                    List.of(),
                    "ALL"
            );
            selectedModel = execResult.selectedModel() != null ? execResult.selectedModel() : "gemini-2.5-flash";
            replyContent = execResult.finalMarkdown();

            StringBuilder citationsJson = new StringBuilder("[");
            if (execResult.verifiedCitations() != null) {
                for (int i = 0; i < execResult.verifiedCitations().size(); i++) {
                    if (i > 0) citationsJson.append(",");
                    String url = execResult.verifiedCitations().get(i);
                    String label = url.replace("https://", "").replace("http://", "").split("/")[0];
                    citationsJson.append("{\"label\":\"").append(label).append("\",\"url\":\"").append(url).append("\"}");
                }
            }
            citationsJson.append("]");
            citationsJsonString = citationsJson.toString();
        }

        MessageEntity userMsg = new MessageEntity(
                UUID.randomUUID(),
                conversationId,
                userId,
                "USER",
                request.content().trim(),
                null,
                "[]"
        );
        messageRepository.save(userMsg);

        MessageEntity assistantMsg = new MessageEntity(
                UUID.randomUUID(),
                conversationId,
                userId,
                "ASSISTANT",
                replyContent,
                selectedModel,
                citationsJsonString
        );
        messageRepository.save(assistantMsg);

        user.setDailyTokensUsed(user.getDailyTokensUsed() + 350);
        userRepository.save(user);

        conv.setLastMessageAt(Instant.now());
        conversationRepository.save(conv);

        return new ChatTurnResponse(conversationId, userMsg, assistantMsg, selectedModel, quotaWarning);
    }

    private boolean isGreetingOrCasual(String query) {
        if (query.matches(".*\\b(alo|alô|hi|hello|hey|chào|chào bạn|chào em|chào bot|chào trợ lý|xin chào|good morning|bạn là ai|bạn tên gì|khỏe không|cảm ơn|thanks|ok|oke|tuyệt vời|tạm biệt|bye)\\b.*")) {
            return true;
        }
        return query.equals("alo") || query.equals("alô") || query.equals("hi") || query.equals("hello")
                || query.equals("hey") || query.equals("chào") || query.equals("ê") || query.equals("bạn ơi")
                || query.equals("ok") || query.equals("cảm ơn") || query.equals("thanks");
    }

    private boolean isVideoIntent(String query) {
        return query.contains("video") || query.contains("shorts") || query.contains("tiktok")
                || query.contains("youtube") || query.contains("stickman") || query.contains("lemo")
                || query.contains("kịch bản") || query.contains("phân cảnh") || query.contains("storyboard")
                || query.contains("làm phim") || query.contains("quay video") || query.contains("dựng video");
    }

    private boolean isVisionIntent(String query) {
        return query.contains("hóa đơn") || query.contains("biên lai") || query.contains("ocr")
                || query.contains("quét ảnh") || query.contains("chi tiêu") || query.contains("vat");
    }

    private boolean isJobsIntent(String query) {
        return query.contains("việc làm") || query.contains("tìm việc") || query.contains("tuyển dụng")
                || query.contains("test game") || query.contains("cày game") || query.contains("remote")
                || query.contains("thực tập");
    }

    private boolean isShoppingIntent(String query) {
        return query.contains("shopee") || query.contains("săn deal") || query.contains("khuyến mãi")
                || query.contains("giá") || query.contains("keychron") || query.contains("bàn phím")
                || query.contains("màn hình 4k");
    }

    private boolean isExplicitAutomationSchedule(String query) {
        boolean hasScheduleAction = query.contains("lên lịch") || query.contains("hẹn giờ")
                || query.contains("tự động gửi") || query.contains("gửi mail") || query.contains("gửi email")
                || query.contains("nhận mail") || query.contains("mỗi sáng") || query.contains("hàng ngày gửi");
        boolean hasTopic = query.contains("tin") || query.contains("bản tin") || query.contains("việc")
                || query.contains("deal") || query.contains("báo cáo") || query.contains("công nghệ")
                || query.contains("game") || query.contains("mã nguồn");
        return hasScheduleAction && hasTopic;
    }
}

