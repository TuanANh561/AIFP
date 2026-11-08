package com.aifp.core.security;

import com.aifp.core.auth.RefreshTokenRepository;
import com.aifp.core.auth.UserEntity;
import com.aifp.core.auth.UserRepository;
import com.aifp.core.common.ApiResponse;
import com.aifp.core.task.AiTaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/security")
public class EmergencyKillSwitchController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AiTaskRepository taskRepository;
    private final SecurityAuditService auditService;

    public EmergencyKillSwitchController(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AiTaskRepository taskRepository,
            SecurityAuditService auditService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.taskRepository = taskRepository;
        this.auditService = auditService;
    }

    public record KillSwitchSummary(
            UUID userId,
            int pausedTasksCount,
            int newTokenVersion,
            Instant triggeredAt
    ) {}

    @PostMapping("/emergency-stop")
    @Transactional
    public ResponseEntity<ApiResponse<KillSwitchSummary>> triggerEmergencyStop(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // 1. Pause all active AI tasks for this user
        int pausedCount = taskRepository.pauseAllActiveTasksForKillSwitch(user.getId());

        // 2. Revoke all refresh tokens and increment tokenVersion to invalidate existing JWTs
        refreshTokenRepository.revokeAllForUser(user.getId());
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // 3. Record immutable audit log
        auditService.logEvent(
                user.getId(),
                "KILL_SWITCH_ACTIVATED",
                "CRITICAL",
                "ACCOUNT",
                user.getId().toString(),
                "{\"pausedTasksCount\":" + pausedCount + ",\"newTokenVersion\":" + user.getTokenVersion() + "}"
        );

        return ResponseEntity.ok(ApiResponse.ok(new KillSwitchSummary(
                user.getId(),
                pausedCount,
                user.getTokenVersion(),
                Instant.now()
        )));
    }
}

