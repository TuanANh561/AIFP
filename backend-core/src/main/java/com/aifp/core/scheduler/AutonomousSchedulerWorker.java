package com.aifp.core.scheduler;

import com.aifp.core.common.AiOrchestratorClient;
import com.aifp.core.common.IdempotencyUtils;
import com.aifp.core.task.AiTaskEntity;
import com.aifp.core.task.AiTaskRepository;
import com.aifp.core.task.TaskExecutionEntity;
import com.aifp.core.task.TaskExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Autonomous Background Scheduler Worker.
 * Executes active scheduled slots autonomously without requiring any user to open a browser or app.
 */
@Component
public class AutonomousSchedulerWorker {

    private static final Logger log = LoggerFactory.getLogger(AutonomousSchedulerWorker.class);

    private final AiTaskRepository taskRepository;
    private final TaskExecutionRepository executionRepository;
    private final IdempotentTaskSchedulerService schedulerService;
    private final AiOrchestratorClient aiOrchestratorClient;
    private final com.aifp.core.notification.LiveNewsCrawlerService liveNewsCrawlerService;
    private final com.aifp.core.notification.LinkValidationService linkValidationService;
    private final boolean schedulerEnabled;

    public AutonomousSchedulerWorker(
            AiTaskRepository taskRepository,
            TaskExecutionRepository executionRepository,
            IdempotentTaskSchedulerService schedulerService,
            AiOrchestratorClient aiOrchestratorClient,
            com.aifp.core.notification.LiveNewsCrawlerService liveNewsCrawlerService,
            com.aifp.core.notification.LinkValidationService linkValidationService,
            @Value("${aifp.scheduler.enabled:true}") boolean schedulerEnabled
    ) {
        this.taskRepository = taskRepository;
        this.executionRepository = executionRepository;
        this.schedulerService = schedulerService;
        this.aiOrchestratorClient = aiOrchestratorClient;
        this.liveNewsCrawlerService = liveNewsCrawlerService;
        this.linkValidationService = linkValidationService;
        this.schedulerEnabled = schedulerEnabled;
    }

    /**
     * Polls active tasks every 60 seconds and autonomously triggers runs for eligible slots.
     */
    @Scheduled(fixedDelayString = "${aifp.scheduler.poll-interval-ms:60000}", initialDelay = 15000)
    public void runAutonomousTaskCycle() {
        if (!schedulerEnabled) {
            return;
        }

        // Keep live news database synced with fresh articles
        try {
            liveNewsCrawlerService.syncLiveFeeds();
        } catch (Exception e) {
            log.warn("[AutonomousWorker] Background news sync: {}", e.getMessage());
        }

        List<AiTaskEntity> activeTasks = taskRepository.findByStatus("ACTIVE");
        if (activeTasks.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        Instant currentSlot = now.truncatedTo(ChronoUnit.HOURS);

        for (AiTaskEntity task : activeTasks) {
            try {
                String idempotencyKey = IdempotencyUtils.computeExecutionIdempotencyKey(task.getId(), currentSlot);
                Optional<TaskExecutionEntity> existing = executionRepository.findByTaskIdAndIdempotencyKey(task.getId(), idempotencyKey);
                if (existing.isPresent()) {
                    continue; // Slot already executed idempotently
                }

                log.info("[AutonomousWorker] Executing scheduled slot for task: {} ({})", task.getTitle(), task.getId());

                // 1. Invoke LangGraph AI Agent to discover live articles
                AiOrchestratorClient.AgentExecutionResult execResult = aiOrchestratorClient.executeAgentTask(
                        task.getUserId(),
                        task.getNaturalLanguagePrompt(),
                        List.of("searxng_search", "rss_fetch", "webpage_extract"),
                        List.of(),
                        task.getDeliveryMode()
                );

                // 2. Build Discovered Items
                List<IdempotentTaskSchedulerService.DiscoveredItem> discoveredItems = new ArrayList<>();
                if (execResult.verifiedCitations() != null && !execResult.verifiedCitations().isEmpty()) {
                    for (String url : execResult.verifiedCitations()) {
                        if (linkValidationService.isUrlLive(url)) {
                            String title = url.replace("https://", "").replace("http://", "").split("/")[0];
                            discoveredItems.add(new IdempotentTaskSchedulerService.DiscoveredItem(title, url));
                        } else {
                            log.warn("[AutonomousWorker] Filtered dead citation URL: {}", url);
                        }
                    }
                }
                if (discoveredItems.isEmpty()) {
                    discoveredItems.add(new IdempotentTaskSchedulerService.DiscoveredItem(
                            task.getTitle(),
                            "https://vnexpress.net/tin-tuc-24h"
                    ));
                }

                // 3. Execute through idempotent scheduler (which dispatches email if delta > 0)
                TaskExecutionEntity execution = schedulerService.executeScheduledSlot(
                        task.getUserId(),
                        task.getId(),
                        currentSlot,
                        now,
                        discoveredItems
                );

                log.info("[AutonomousWorker] Task execution completed: {} -> Status: {}", task.getTitle(), execution.getStatus());
            } catch (Exception ex) {
                log.error("[AutonomousWorker] Failed executing task {}: {}", task.getTitle(), ex.getMessage());
            }
        }
    }
}

