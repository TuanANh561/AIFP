package com.aifp.core.task;

import com.aifp.core.common.AiOrchestratorClient;
import com.aifp.core.common.ApiResponse;
import com.aifp.core.scheduler.IdempotentTaskSchedulerService;
import com.aifp.core.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final AiTaskRepository taskRepository;
    private final TaskExecutionRepository executionRepository;
    private final AiOrchestratorClient aiOrchestratorClient;
    private final IdempotentTaskSchedulerService schedulerService;

    public TaskController(
            AiTaskRepository taskRepository,
            TaskExecutionRepository executionRepository,
            AiOrchestratorClient aiOrchestratorClient,
            IdempotentTaskSchedulerService schedulerService
    ) {
        this.taskRepository = taskRepository;
        this.executionRepository = executionRepository;
        this.aiOrchestratorClient = aiOrchestratorClient;
        this.schedulerService = schedulerService;
    }

    public record ParseTaskPromptRequest(
            @NotBlank @Size(max = 4000) String naturalLanguagePrompt,
            String timezone
    ) {}

    public record CreateTaskRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 4000) String naturalLanguagePrompt,
            String deliveryMode
    ) {}

    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<AiOrchestratorClient.ParsedTaskProposal>> parseTaskPrompt(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ParseTaskPromptRequest request
    ) {
        AiOrchestratorClient.ParsedTaskProposal proposal = aiOrchestratorClient.parseNaturalLanguageTask(
                principal.userId(),
                request.naturalLanguagePrompt(),
                request.timezone()
        );
        return ResponseEntity.ok(ApiResponse.ok(proposal));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<AiTaskEntity>> createTask(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        AiTaskEntity task = new AiTaskEntity(
                UUID.randomUUID(),
                principal.userId(),
                request.title().trim(),
                request.naturalLanguagePrompt().trim(),
                request.deliveryMode()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(taskRepository.save(task)));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<AiTaskEntity>>> listTasks(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                taskRepository.findByUserIdOrderByCreatedAtDesc(principal.userId())
        ));
    }

    @GetMapping("/{taskId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<AiTaskEntity>> getTask(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID taskId
    ) {
        AiTaskEntity task = taskRepository.findByIdAndUserId(taskId, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return ResponseEntity.ok(ApiResponse.ok(task));
    }

    @GetMapping("/{taskId}/executions")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<TaskExecutionEntity>>> listTaskExecutions(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID taskId
    ) {
        taskRepository.findByIdAndUserId(taskId, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return ResponseEntity.ok(ApiResponse.ok(
                executionRepository.findTop30ByTaskIdAndUserIdOrderByStartedAtDesc(taskId, principal.userId())
        ));
    }

    @PatchMapping("/{taskId}/toggle")
    @Transactional
    public ResponseEntity<ApiResponse<AiTaskEntity>> toggleTaskStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID taskId
    ) {
        AiTaskEntity task = taskRepository.findByIdAndUserId(taskId, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setStatus("ACTIVE".equals(task.getStatus()) ? "PAUSED" : "ACTIVE");
        task.setUpdatedAt(Instant.now());
        return ResponseEntity.ok(ApiResponse.ok(taskRepository.save(task)));
    }

    @PostMapping("/{taskId}/run-now")
    public ResponseEntity<ApiResponse<TaskExecutionEntity>> runTaskNow(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID taskId
    ) {
        AiTaskEntity task = taskRepository.findByIdAndUserId(taskId, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Instant now = Instant.now();

        // 1. Invoke LangGraph Agent to discover live articles
        AiOrchestratorClient.AgentExecutionResult execResult = aiOrchestratorClient.executeAgentTask(
                principal.userId(),
                task.getNaturalLanguagePrompt(),
                List.of("searxng_search", "rss_fetch", "webpage_extract"),
                List.of(),
                task.getDeliveryMode()
        );

        // 2. Build Discovered Items from verified citations
        List<IdempotentTaskSchedulerService.DiscoveredItem> discoveredItems = new java.util.ArrayList<>();
        if (execResult.verifiedCitations() != null && !execResult.verifiedCitations().isEmpty()) {
            for (String url : execResult.verifiedCitations()) {
                String title = url.replace("https://", "").replace("http://", "").split("/")[0];
                discoveredItems.add(new IdempotentTaskSchedulerService.DiscoveredItem(title, url));
            }
        }
        if (discoveredItems.isEmpty()) {
            discoveredItems.add(new IdempotentTaskSchedulerService.DiscoveredItem(
                    task.getTitle(),
                    "https://vnexpress.net/tin-moi-nhat"
            ));
        }

        // 3. Execute through IdempotentTaskSchedulerService
        TaskExecutionEntity execution = schedulerService.executeScheduledSlot(
                principal.userId(),
                taskId,
                now,
                now,
                discoveredItems
        );
        return ResponseEntity.ok(ApiResponse.ok(execution));
    }
}
