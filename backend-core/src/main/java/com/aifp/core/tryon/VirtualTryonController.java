package com.aifp.core.tryon;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.tool.ToolPermissionGateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tryon")
public class VirtualTryonController {

    private final VirtualTryonSessionRepository tryonRepository;
    private final ToolPermissionGateService toolPermissionGateService;

    public VirtualTryonController(
            VirtualTryonSessionRepository tryonRepository,
            ToolPermissionGateService toolPermissionGateService
    ) {
        this.tryonRepository = tryonRepository;
        this.toolPermissionGateService = toolPermissionGateService;
    }

    public record TryonRequest(
            @NotBlank String garmentTitle,
            @NotBlank String garmentCategory, // TOP, BOTTOM, DRESS, FULL_OUTFIT
            String garmentImageUrl,
            String userPhotoBase64
    ) {}

    @GetMapping("/sessions")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<VirtualTryonSessionEntity>>> listMySessions(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                tryonRepository.findByUserIdOrderByCreatedAtDesc(principal.userId())
        ));
    }

    @PostMapping("/synthesize")
    @Transactional
    public ResponseEntity<ApiResponse<VirtualTryonSessionEntity>> synthesizeTryon(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TryonRequest request
    ) {
        // Enforce Level 2 tool permission gate (virtual_tryon_synthesize)
        toolPermissionGateService.evaluateToolCall(
                principal.userId(),
                null,
                "virtual_tryon_synthesize",
                "{\"garment\":\"" + request.garmentTitle().replace("\"", "\\\"") + "\",\"category\":\"" + request.garmentCategory() + "\"}",
                "{}"
        );

        // Strict 24h Privacy Auto-Expiry
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
        String resultMockUrl = "https://aifp.local/cdn/tryon/synthesized_" + UUID.randomUUID() + ".png";

        VirtualTryonSessionEntity session = new VirtualTryonSessionEntity(
                UUID.randomUUID(),
                principal.userId(),
                request.garmentTitle().trim(),
                request.garmentCategory().trim().toUpperCase(),
                request.garmentImageUrl() != null ? request.garmentImageUrl().trim() : "https://aifp.local/sample_garment.png",
                resultMockUrl,
                expiresAt
        );

        VirtualTryonSessionEntity saved = tryonRepository.save(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    @DeleteMapping("/sessions/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        VirtualTryonSessionEntity session = tryonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUserId().equals(principal.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: tenant isolation violation");
        }

        tryonRepository.delete(session);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

