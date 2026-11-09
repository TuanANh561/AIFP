package com.aifp.core.task;

import com.aifp.core.auth.UserEntity;
import com.aifp.core.auth.UserProfileEntity;
import com.aifp.core.auth.UserProfileRepository;
import com.aifp.core.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class TaskInitializerService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TaskInitializerService.class);
    public static final UUID DEFAULT_SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String DEFAULT_SYSTEM_EMAIL = "alex.dev@aifp.local";

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AiTaskRepository taskRepository;
    private final String ownerNotificationEmail;

    public TaskInitializerService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            AiTaskRepository taskRepository,
            @org.springframework.beans.factory.annotation.Value("${aifp.owner-email:}") String ownerNotificationEmail
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.taskRepository = taskRepository;
        this.ownerNotificationEmail = ownerNotificationEmail == null ? "" : ownerNotificationEmail.trim();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            // 1. Ensure default system user exists
            UserEntity user = userRepository.findById(DEFAULT_SYSTEM_USER_ID).orElseGet(() -> {
                UserEntity newUser = new UserEntity(
                        DEFAULT_SYSTEM_USER_ID,
                        DEFAULT_SYSTEM_EMAIL,
                        "$argon2id$v=19$m=65536,t=3,p=4$dummy_hash_for_system_seed$placeholder"
                );
                newUser.setEmailVerified(true);
                return userRepository.save(newUser);
            });

            // 2. Ensure profile exists
            if (!profileRepository.existsById(user.getId())) {
                UserProfileEntity profile = new UserProfileEntity(user.getId(), "Tuấn Anh (Kỹ sư)", "DEVELOPER");
                profile.setNotificationEmail(ownerNotificationEmail.isEmpty() ? DEFAULT_SYSTEM_EMAIL : ownerNotificationEmail);
                profileRepository.save(profile);
            } else if (!ownerNotificationEmail.isEmpty()) {
                // Deployment: AIFP_OWNER_EMAIL always wins so scheduled digests reach the real inbox
                profileRepository.findById(user.getId()).ifPresent(p -> {
                    if (!ownerNotificationEmail.equals(p.getNotificationEmail())) {
                        p.setNotificationEmail(ownerNotificationEmail);
                        profileRepository.save(p);
                    }
                });
            }

            // 3. Seed default topic channels as persistent autonomous tasks if none exist
            List<AiTaskEntity> existingTasks = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            if (existingTasks.isEmpty()) {
                log.info("[TaskInitializer] Seeding default topic automation tasks for system user...");

                AiTaskEntity taskShopee = new AiTaskEntity(
                        UUID.fromString("33333333-3333-3333-3333-333333333301"),
                        user.getId(),
                        "Săn Deal & Khuyến mãi Shopee (Keychron, Màn hình 4K, Tai nghe ANC)",
                        "Cào quét các chương trình giảm giá flash sale Shopee Mall, tìm kiếm bàn phím cơ Keychron, màn hình 4K LG và phụ kiện công nghệ",
                        "DELTA_ONLY"
                );

                AiTaskEntity taskJobs = new AiTaskEntity(
                        UUID.fromString("33333333-3333-3333-3333-333333333302"),
                        user.getId(),
                        "Tuyển dụng Kỹ sư IT & Tech Jobs (Flutter, Spring Boot, AI Engineer)",
                        "Tìm kiếm cơ hội việc làm tuyển dụng lập trình viên Flutter, Spring Boot Java và AI LangGraph trên ITViec và TopCV",
                        "DELTA_ONLY"
                );

                AiTaskEntity taskTech = new AiTaskEntity(
                        UUID.fromString("33333333-3333-3333-3333-333333333303"),
                        user.getId(),
                        "Tin tức Công nghệ & AI Mới nhất (VnExpress, Dân Trí, HackerNews)",
                        "Điểm tin thời sự công nghệ, trí tuệ nhân tạo và thiết bị mới nhất từ VnExpress Số Hóa",
                        "DELTA_ONLY"
                );

                AiTaskEntity taskFinance = new AiTaskEntity(
                        UUID.fromString("33333333-3333-3333-3333-333333333304"),
                        user.getId(),
                        "Thị trường Tài chính & Đầu tư (Kinh tế vĩ mô & Chứng khoán)",
                        "Tổng hợp diễn biến thị trường chứng khoán VN-Index và tin tức tài chính kinh doanh",
                        "DELTA_ONLY"
                );
                taskFinance.setStatus("PAUSED");

                AiTaskEntity taskSocial = new AiTaskEntity(
                        UUID.fromString("33333333-3333-3333-3333-333333333305"),
                        user.getId(),
                        "Cộng đồng Facebook & Diễn đàn Lập trình viên",
                        "Tổng hợp các thảo luận kỹ thuật hay trong cộng đồng lập trình viên Việt Nam",
                        "DELTA_ONLY"
                );
                taskSocial.setStatus("PAUSED");

                AiTaskEntity taskAiEvolution = new AiTaskEntity(
                        UUID.fromString("33333333-3333-3333-3333-333333333306"),
                        user.getId(),
                        "Báo cáo Tiến hóa AI & Lộ trình Nâng cấp Hệ thống AIFP (Hàng ngày)",
                        "Tự động phân tích đột phá AI mới nhất (arXiv, Hugging Face, MCP, On-device SLM), đối chiếu quá khứ - hiện tại - tương lai của AIFP và đề xuất lộ trình cải tiến hệ thống.",
                        "DELTA_ONLY"
                );

                taskRepository.saveAll(List.of(taskShopee, taskJobs, taskTech, taskFinance, taskSocial, taskAiEvolution));
                log.info("[TaskInitializer] Successfully seeded 6 autonomous topic tasks.");
            } else {
                UUID aiEvolutionId = UUID.fromString("33333333-3333-3333-3333-333333333306");
                if (!taskRepository.existsById(aiEvolutionId)) {
                    AiTaskEntity taskAiEvolution = new AiTaskEntity(
                            aiEvolutionId,
                            user.getId(),
                            "Báo cáo Tiến hóa AI & Lộ trình Nâng cấp Hệ thống AIFP (Hàng ngày)",
                            "Tự động phân tích đột phá AI mới nhất (arXiv, Hugging Face, MCP, On-device SLM), đối chiếu quá khứ - hiện tại - tương lai của AIFP và đề xuất lộ trình cải tiến hệ thống.",
                            "DELTA_ONLY"
                    );
                    taskRepository.save(taskAiEvolution);
                    log.info("[TaskInitializer] Seeded AI Evolution autonomous task for existing database.");
                }

                UUID creatorIdeasId = UUID.fromString("33333333-3333-3333-3333-333333333307");
                if (!taskRepository.existsById(creatorIdeasId)) {
                    AiTaskEntity taskCreatorIdeas = new AiTaskEntity(
                            creatorIdeasId,
                            user.getId(),
                            "Ý Tưởng Kiếm Tiền, Làm Game & Sáng Tạo Video Đa Nền Tảng (Aura)",
                            "Tự động gửi email phân tích các ý tưởng kiếm tiền thực tế: phát triển game indie mini bằng Godot, kịch bản video YouTube Gaming thử thách kết hợp đòn bẩy AI, và chiến lược phân bổ đa nền tảng tối đa hóa doanh thu.",
                            "DELTA_ONLY"
                    );
                    taskRepository.save(taskCreatorIdeas);
                    log.info("[TaskInitializer] Seeded Creator Ideas autonomous task for existing database.");
                }
            }
        } catch (Exception ex) {
            log.warn("[TaskInitializer] Initial seeding skipped or already present: {}", ex.getMessage());
        }
    }

    @Transactional
    public void seedDefaultTasksForUser(UUID userId) {
        if (taskRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            AiTaskEntity taskShopee = new AiTaskEntity(
                    UUID.randomUUID(),
                    userId,
                    "Săn Deal & Khuyến mãi Shopee (Keychron, Màn hình 4K, Tai nghe ANC)",
                    "Cào quét các chương trình giảm giá flash sale Shopee Mall, tìm kiếm bàn phím cơ Keychron, màn hình 4K LG và phụ kiện công nghệ",
                    "DELTA_ONLY"
            );
            AiTaskEntity taskJobs = new AiTaskEntity(
                    UUID.randomUUID(),
                    userId,
                    "Tuyển dụng Kỹ sư IT & Tech Jobs (Flutter, Spring Boot, AI Engineer)",
                    "Tìm kiếm cơ hội việc làm tuyển dụng lập trình viên Flutter, Spring Boot Java và AI LangGraph trên ITViec và TopCV",
                    "DELTA_ONLY"
            );
            AiTaskEntity taskTech = new AiTaskEntity(
                    UUID.randomUUID(),
                    userId,
                    "Tin tức Công nghệ & AI Mới nhất (VnExpress, Dân Trí, HackerNews)",
                    "Điểm tin thời sự công nghệ, trí tuệ nhân tạo và thiết bị mới nhất từ VnExpress Số Hóa",
                    "DELTA_ONLY"
            );
            AiTaskEntity taskAiEvolution = new AiTaskEntity(
                    UUID.randomUUID(),
                    userId,
                    "Báo cáo Tiến hóa AI & Lộ trình Nâng cấp Hệ thống AIFP (Hàng ngày)",
                    "Tự động phân tích đột phá AI mới nhất (arXiv, Hugging Face, MCP, On-device SLM), đối chiếu quá khứ - hiện tại - tương lai của AIFP và đề xuất lộ trình cải tiến hệ thống.",
                    "DELTA_ONLY"
            );
            taskRepository.saveAll(List.of(taskShopee, taskJobs, taskTech, taskAiEvolution));
            log.info("[TaskInitializer] Seeded default topic tasks for new user: {}", userId);
        }
    }
}

