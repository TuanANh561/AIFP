package com.aifp.core.tool;

import com.aifp.core.security.SecurityAuditService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ToolPermissionGateService {

    private final AiToolRepository toolRepository;
    private final TaskApprovalRequestRepository approvalRepository;
    private final SecurityAuditService auditService;

    public ToolPermissionGateService(
            AiToolRepository toolRepository,
            TaskApprovalRequestRepository approvalRepository,
            SecurityAuditService auditService
    ) {
        this.toolRepository = toolRepository;
        this.approvalRepository = approvalRepository;
        this.auditService = auditService;
    }

    public record GateDecision(
            String status, // ALLOWED, WAITING_FOR_APPROVAL, DENIED
            short permissionLevel,
            UUID approvalRequestId
    ) {}

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public GateDecision evaluateToolCall(
            UUID userId,
            UUID executionId,
            String toolCode,
            String proposedPayloadJson,
            String checkpointJson
    ) {
        AiToolEntity tool = toolRepository.findByCode(toolCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Unregistered AI tool: " + toolCode));

        short level = tool.getPermissionLevel();
        if (level >= 4 || !tool.isEnabledGlobally()) {
            auditService.logEvent(
                    userId,
                    "TOOL_DENIED_LEVEL4",
                    "HIGH",
                    "AI_TOOL",
                    toolCode,
                    "{\"reason\":\"Level 4 restricted or globally disabled tool blocked\"}"
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tool execution blocked by Level 4 Security Gate: " + toolCode);
        }

        if (level == 3) {
            TaskApprovalRequestEntity req = new TaskApprovalRequestEntity(
                    UUID.randomUUID(),
                    executionId,
                    userId,
                    toolCode,
                    proposedPayloadJson,
                    checkpointJson,
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );
            approvalRepository.save(req);
            auditService.logEvent(
                    userId,
                    "TOOL_PAUSED_FOR_HITL_APPROVAL",
                    "INFO",
                    "TASK_APPROVAL",
                    req.getId().toString(),
                    "{\"tool\":\"" + toolCode + "\"}"
            );
            return new GateDecision("WAITING_FOR_APPROVAL", level, req.getId());
        }

        if (level == 2) {
            auditService.logEvent(userId, "TOOL_EXECUTED_LEVEL2", "INFO", "AI_TOOL", toolCode, "{}");
        }
        return new GateDecision("ALLOWED", level, null);
    }
}

