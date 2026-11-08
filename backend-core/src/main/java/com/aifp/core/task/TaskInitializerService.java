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
    public static final String DEFAULT_SYSTEM_EMAIL = "nguyentuananh11a1.lhpt@gmail.com";

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AiTaskRepository taskRepository;

    public TaskInitializerService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            AiTaskRepository taskRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.taskRepository = taskRepository;
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
                profile.setNotificationEmail(DEFAULT_SYSTEM_EMAIL);
                profileRepository.save(profile);
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

                taskRepository.saveAll(List.of(taskShopee, taskJobs, taskTech, taskFinance, taskSocial));
                log.info("[TaskInitializer] Successfully seeded 5 autonomous topic tasks.");
            }
        } catch (Exception ex) {
            log.warn("[TaskInitializer] Initial seeding skipped or already present: {}", ex.getMessage());
        }
    }
}

