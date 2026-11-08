package com.aifp.core.tool;

import com.aifp.core.calendar.CalendarEventRepository;
import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.security.SecurityAuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final TaskApprovalRequestRepository approvalRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final SecurityAuditService auditService;

    public ApprovalController(
            TaskApprovalRequestRepository approvalRepository,
            CalendarEventRepository calendarEventRepository,
            SecurityAuditService auditService
    ) {
        this.approvalRepository = approvalRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.auditService = auditService;
    }

    public record ApprovalDecisionRequest(@NotBlank String decision) {} // APPROVED or REJECTED

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<TaskApprovalRequestEntity>>> listPendingApprovals(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                approvalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(principal.userId(), "PENDING")
        ));
    }

    @PostMapping("/{approvalId}/decide")
    @Transactional
    public ResponseEntity<ApiResponse<TaskApprovalRequestEntity>> decideApproval(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID approvalId,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        TaskApprovalRequestEntity entity = approvalRepository.findByIdAndUserId(approvalId, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval request not found"));

        if (!"PENDING".equals(entity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Approval request already decided");
        }

        String normalized = request.decision().trim().toUpperCase();
        if (!"APPROVED".equals(normalized) && !"REJECTED".equals(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVED or REJECTED");
        }

        entity.setStatus(normalized);
        entity.setDecidedAt(Instant.now());
        TaskApprovalRequestEntity saved = approvalRepository.save(entity);

        // If this approval request was created for a Calendar Assistant event (calendar_event_write),
        // transition the linked calendar event status to CONFIRMED or CANCELLED automatically.
        calendarEventRepository.findByApprovalRequestIdAndUserId(approvalId, principal.userId())
                .ifPresent(calEvent -> {
                    calEvent.setStatus("APPROVED".equals(normalized) ? "CONFIRMED" : "CANCELLED");
                    calendarEventRepository.save(calEvent);
                });

        auditService.logEvent(
                principal.userId(),
                "HITL_APPROVAL_" + normalized,
                "INFO",
                "TASK_APPROVAL",
                approvalId.toString(),
                "{\"tool\":\"" + entity.getToolCode() + "\"}"
        );
        return ResponseEntity.ok(ApiResponse.ok(saved));
    }
}

