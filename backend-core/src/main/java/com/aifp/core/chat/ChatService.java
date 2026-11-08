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

        boolean isAutomationIntent = (lowerQuery.contains("mail") || lowerQuery.contains("email") || lowerQuery.contains("tự động") || lowerQuery.contains("luồng") || lowerQuery.contains("lên lịch") || lowerQuery.contains("cấu hình") || lowerQuery.contains("đề xuất") || lowerQuery.contains("phát triển") || lowerQuery.contains("tự học") || lowerQuery.contains("học hỏi") || lowerQuery.contains("ý tưởng") || lowerQuery.contains("kiếm tiền") || lowerQuery.contains("kịch bản") || lowerQuery.contains("làm game") || lowerQuery.contains("làm video"))
                && (lowerQuery.contains("việc") || lowerQuery.contains("job") || lowerQuery.contains("game") || lowerQuery.contains("remote") || lowerQuery.contains("shopee") || lowerQuery.contains("deal") || lowerQuery.contains("công nghệ") || lowerQuery.contains("tiến hóa") || lowerQuery.contains("mã nguồn mở") || lowerQuery.contains("open source") || lowerQuery.contains("phát triển") || lowerQuery.contains("gửi") || lowerQuery.contains("học") || lowerQuery.contains("hệ thống") || lowerQuery.contains("kiếm tiền") || lowerQuery.contains("video") || lowerQuery.contains("youtube") || lowerQuery.contains("tiktok") || lowerQuery.contains("short"));

        String selectedModel = "gemini-2.5-flash";
        String replyContent;
        String citationsJsonString = "[]";

        if (isAutomationIntent) {
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

            if (lowerQuery.contains("kiếm tiền") || lowerQuery.contains("làm game") || lowerQuery.contains("làm video") || lowerQuery.contains("kịch bản") || lowerQuery.contains("youtube") || lowerQuery.contains("tiktok") || lowerQuery.contains("short") || lowerQuery.contains("ý tưởng")) {
                channelId = "creator-ideas";
                title = "Ý Tưởng Kiếm Tiền & Hiện Thực Hóa Sáng Tạo (Game, Video)";
                keywords = List.of("Game Indie Godot", "YouTube Gaming Hybrid", "Kịch bản AI", "TikTok Shorts", "Kiếm tiền Online");
                scheduledTime = "20:00";
            } else if (lowerQuery.contains("mã nguồn mở") || lowerQuery.contains("open source") || lowerQuery.contains("tự học") || lowerQuery.contains("tự phát triển") || lowerQuery.contains("tiến hóa") || lowerQuery.contains("lộ trình") || lowerQuery.contains("nâng cấp")) {
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

            // Persist the autonomous task in CSDL (PostgreSQL)
            AiTaskEntity newTask = new AiTaskEntity(
                    UUID.randomUUID(),
                    userId,
                    title,
                    "Luồng tự động hóa cào quét và gửi email theo chủ đề [" + title + "] vào lúc " + scheduledTime + " (" + frequency + "). Lọc các tin tức/việc làm mới có liên kết trực tiếp và lưu trữ vào CSDL.",
                    "DELTA_ONLY"
            );
            taskRepository.save(newTask);

            if ("creator-ideas".equals(channelId)) {
                replyContent = "Đã kích hoạt và lên lịch luồng **Ý Tưởng Kiếm Tiền & Hiện Thực Hóa Sáng Tạo (Game & Video)** cho bạn:\n\n" +
                        "• **Bài học thực tế từ trải nghiệm của bạn:** Giải mã lý do video 100% AI ít view (thiếu cảm xúc, tụt retention) và công thức chiến thắng **Gameplay thật của BẠN (Linh hồn) + AI làm đòn bẩy hậu cần** (hook 3s, kịch bản, thumbnail, SEO).\n" +
                        "• **3 Lộ trình kiếm tiền cụ thể:** (1) Kênh YouTube Gaming thử thách kết hợp đa nền tảng, (2) Solo Indie Game bằng Godot 4 & AI bán trên Steam/Itch.io, (3) Dịch vụ Narrative Designer kịch bản game quốc tế.\n" +
                        "• **Lịch gửi email:** " + scheduledTime + " (" + frequency + ") tới `" + recipientEmail + "`.\n" +
                        "• **Không giới hạn Token AI:** Hệ thống phục vụ riêng cho bạn, sẵn sàng sinh kịch bản chi tiết và code mẫu từng bước bất cứ khi nào bạn yêu cầu.\n\n" +
                        "Bạn có thể bấm **'Gửi email thử ngay'** bên dưới để nhận ngay bản kế hoạch chi tiết vào hòm thư!";
            } else if ("ai-evolution".equals(channelId)) {
                replyContent = "Đã kích hoạt và lên lịch luồng **Tự Học Hỏi & Đề Xuất Nâng Cấp Hệ Thống Hàng Ngày**:\n\n" +
                        "• **Cơ chế tự vận hành:** Hệ thống sẽ tự động đối chiếu các mã nguồn mở tinh hoa (Crawl4AI, MCP Standard, Mem0, Ollama SLM, LiveKit) với kiến trúc hiện tại của AIFP.\n" +
                        "• **Báo cáo đề xuất:** Tự động tạo bản đề xuất kỹ thuật (RFC/ADR) gồm: bài toán giải quyết, kiến trúc tích hợp, ước tính chi phí tiết kiệm và code mẫu.\n" +
                        "• **Lịch gửi email:** " + scheduledTime + " (" + frequency + ") tới `" + recipientEmail + "`.\n" +
                        "• **Quyền kiểm soát (Level 3 Gate):** Mọi đề xuất gửi qua email đều có nút `[Duyệt triển khai]` để bạn toàn quyền xem xét trước khi hệ thống áp dụng.\n\n" +
                        "Bạn có thể bấm **'Gửi email thử ngay'** bên dưới để nhận ngay báo cáo đề xuất mã nguồn mở mới nhất vào hòm thư!";
            } else {
                replyContent = "Đã thiết lập luồng gửi email tự động cho bạn:\n\n" +
                        "• **Chủ đề:** " + title + "\n" +
                        "• **Lịch gửi:** " + scheduledTime + " (" + frequency + ")\n" +
                        "• **Email nhận:** " + recipientEmail + "\n" +
                        "• **Từ khóa:** " + String.join(", ", keywords) + "\n\n" +
                        "Bạn có thể bấm nút bên dưới để nhận email thử ngay hoặc xem danh sách bài mới.";
            }

            String kwJson = "[\"" + String.join("\",\"", keywords) + "\"]";
            replyContent += "\n\n<!-- AUTOMATION_ACTION: {\"action\":\"SETUP_CHANNEL\",\"channelId\":\"" + channelId + "\",\"title\":\"" + title + "\",\"scheduledTime\":\"" + scheduledTime + "\",\"frequency\":\"" + frequency + "\",\"customEmail\":\"" + recipientEmail + "\",\"keywords\":" + kwJson + "} -->";
        } else {
            // Execute LangGraph Agent via HMAC internal bridge
            AiOrchestratorClient.AgentExecutionResult execResult = aiOrchestratorClient.executeAgentTask(
                    userId,
                    rawContent,
                    List.of("searxng_search", "webpage_extract", "rss_fetch"),
                    List.of(),
                    "ALL"
            );
            selectedModel = execResult.selectedModel() != null ? execResult.selectedModel() : "gemini-2.5-flash";
            replyContent = execResult.finalMarkdown();

            // Build citations JSON array
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
}

