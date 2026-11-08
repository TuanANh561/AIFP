package com.aifp.core.scheduler;

import com.aifp.core.common.IdempotencyUtils;
import com.aifp.core.notification.EmailCenterService;
import com.aifp.core.notification.NotificationCenterService;
import com.aifp.core.task.AiTaskEntity;
import com.aifp.core.task.AiTaskRepository;
import com.aifp.core.task.TaskExecutionEntity;
import com.aifp.core.task.TaskExecutionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class IdempotentTaskSchedulerService {

    private static final Duration MISFIRE_STALE_WINDOW = Duration.ofHours(2);

    private final AiTaskRepository taskRepository;
    private final TaskExecutionRepository executionRepository;
    private final EmailCenterService emailCenterService;
    private final NotificationCenterService notificationCenterService;

    public IdempotentTaskSchedulerService(
            AiTaskRepository taskRepository,
            TaskExecutionRepository executionRepository,
            EmailCenterService emailCenterService,
            NotificationCenterService notificationCenterService
    ) {
        this.taskRepository = taskRepository;
        this.executionRepository = executionRepository;
        this.emailCenterService = emailCenterService;
        this.notificationCenterService = notificationCenterService;
    }

    public record DiscoveredItem(String title, String canonicalUrl) {}

    /**
     * Executes a scheduled task slot with:
     * 1. Deterministic Idempotency Key (SHA256(task_id + scheduled_slot_utc)).
     * 2. Misfire Window Check (FIRE_ONCE_WITHIN_WINDOW: skips if delayed > 2 hours).
     * 3. Delta Fingerprinting (DELTA_ONLY mode skips email/push if 0 new items compared to prior runs).
     * 4. Idempotent Email & Web Push Notification dispatch.
     */
    @Transactional
    public TaskExecutionEntity executeScheduledSlot(
            UUID userId,
            UUID taskId,
            Instant scheduledSlotUtc,
            Instant currentEvaluationTimeUtc,
            List<DiscoveredItem> discoveredItems
    ) {
        AiTaskEntity task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!"ACTIVE".equals(task.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Task is not ACTIVE: " + task.getStatus());
        }

        String idempotencyKey = IdempotencyUtils.computeExecutionIdempotencyKey(taskId, scheduledSlotUtc);
        Optional<TaskExecutionEntity> existingExec = executionRepository.findByTaskIdAndIdempotencyKey(taskId, idempotencyKey);
        if (existingExec.isPresent()) {
            // Idempotent return: slot already executed
            return existingExec.get();
        }

        // Check Misfire Policy (FIRE_ONCE_WITHIN_WINDOW < 2 hours)
        Duration delay = Duration.between(scheduledSlotUtc, currentEvaluationTimeUtc);
        if (delay.compareTo(MISFIRE_STALE_WINDOW) > 0) {
            TaskExecutionEntity skipped = new TaskExecutionEntity(
                    UUID.randomUUID(),
                    taskId,
                    userId,
                    idempotencyKey,
                    "SCHEDULED",
                    "SKIPPED_STALE"
            );
            skipped.setResultSummary("Bỏ qua slot trễ quá 2 giờ theo chính sách FIRE_ONCE_WITHIN_WINDOW.");
            skipped.setCompletedAt(currentEvaluationTimeUtc);
            return executionRepository.save(skipped);
        }

        // Collect prior fingerprints from the last 30 runs of this task
        List<TaskExecutionEntity> priorRuns = executionRepository.findTop30ByTaskIdAndUserIdOrderByStartedAtDesc(taskId, userId);
        Set<String> seenFingerprints = new HashSet<>();
        for (TaskExecutionEntity prior : priorRuns) {
            String fpJson = prior.getContentFingerprintsJson();
            if (fpJson != null && fpJson.length() > 2) {
                for (String token : fpJson.replace("[", "").replace("]", "").replace("\"", "").split(",")) {
                    if (!token.isBlank()) {
                        seenFingerprints.add(token.trim());
                    }
                }
            }
        }

        List<DiscoveredItem> deltaItems = new ArrayList<>();
        List<String> newFingerprints = new ArrayList<>();
        for (DiscoveredItem item : discoveredItems) {
            String fp = IdempotencyUtils.computeContentFingerprint(item.canonicalUrl(), item.title());
            if (!seenFingerprints.contains(fp)) {
                deltaItems.add(item);
                newFingerprints.add(fp);
            }
        }

        if ("DELTA_ONLY".equals(task.getDeliveryMode()) && deltaItems.isEmpty()) {
            TaskExecutionEntity noDelta = new TaskExecutionEntity(
                    UUID.randomUUID(),
                    taskId,
                    userId,
                    idempotencyKey,
                    "SCHEDULED",
                    "COMPLETED_NO_DELTA"
            );
            noDelta.setResultSummary("Không có tin mới phát sinh (0 delta).");
            noDelta.setCompletedAt(currentEvaluationTimeUtc);
            return executionRepository.save(noDelta);
        }

        String fpArrayJson = "[\"" + String.join("\",\"", newFingerprints) + "\"]";
        String summary = "Tìm thấy " + deltaItems.size() + " kết quả mới đã kiểm chứng nguồn cho: " + task.getTitle();

        TaskExecutionEntity completed = new TaskExecutionEntity(
                UUID.randomUUID(),
                taskId,
                userId,
                idempotencyKey,
                "SCHEDULED",
                "COMPLETED"
        );
        completed.setContentFingerprintsJson(fpArrayJson);
        completed.setResultSummary(summary);
        completed.setCompletedAt(currentEvaluationTimeUtc);
        TaskExecutionEntity savedExec = executionRepository.save(completed);

        // Dispatch idempotent email & notification
        emailCenterService.sendIdempotentSelfDigest(
                userId,
                savedExec.getId(),
                "[AIFP] " + task.getTitle() + " (" + deltaItems.size() + " tin mới)",
                summary
        );
        notificationCenterService.dispatchNotification(
                userId,
                savedExec.getId(),
                "TASK_RESULT",
                task.getTitle(),
                summary
        );

        return savedExec;
    }
}

