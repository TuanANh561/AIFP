package com.aifp.core.tool;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.security.SecurityAuditLogEntity;
import com.aifp.core.security.SecurityAuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ToolRegistryController {

    private final AiToolRepository toolRepository;
    private final SecurityAuditLogRepository auditLogRepository;

    public ToolRegistryController(
            AiToolRepository toolRepository,
            SecurityAuditLogRepository auditLogRepository
    ) {
        this.toolRepository = toolRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/tools")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<AiToolEntity>>> listTools() {
        List<AiToolEntity> sorted = toolRepository.findAll().stream()
                .sorted(Comparator.comparingInt(AiToolEntity::getPermissionLevel).thenComparing(AiToolEntity::getCode))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(sorted));
    }

    @GetMapping("/security/audit-logs")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<SecurityAuditLogEntity>>> listMyAuditLogs(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditLogRepository.findTop20ByUserIdOrderByCreatedAtDesc(principal.userId())
        ));
    }
}

