package com.aifp.core.pccompanion;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pc-companion")
public class PcCompanionController {

    private final PcCompanionDeviceRepository deviceRepository;
    private final ToolPermissionGateService toolPermissionGateService;

    public PcCompanionController(
            PcCompanionDeviceRepository deviceRepository,
            ToolPermissionGateService toolPermissionGateService
    ) {
        this.deviceRepository = deviceRepository;
        this.toolPermissionGateService = toolPermissionGateService;
    }

    public record PairDeviceRequest(
            @NotBlank String deviceName,
            @NotBlank String osType,
            @NotBlank String sandboxFolderPath
    ) {}

    public record PairDeviceResponse(
            UUID deviceId,
            String deviceName,
            String osType,
            String sandboxFolderPath,
            String pairingToken,
            String status
    ) {}

    @GetMapping("/devices")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PcCompanionDeviceEntity>>> listDevices(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                deviceRepository.findByUserIdOrderByCreatedAtDesc(principal.userId())
        ));
    }

    @PostMapping("/pair")
    @Transactional
    public ResponseEntity<ApiResponse<PairDeviceResponse>> pairDevice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody PairDeviceRequest request
    ) {
        // Strict Sandbox Traversal Defense: Deny any '..' or forbidden root paths
        String path = request.sandboxFolderPath().trim();
        if (path.contains("..") || path.startsWith("/etc") || path.toLowerCase().startsWith("c:\\windows")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sandbox path: path traversal detected");
        }

        // Level 3 Tool Permission Gate (pc_companion_sync)
        toolPermissionGateService.evaluateToolCall(
                principal.userId(),
                null,
                "pc_companion_sync",
                "{\"device\":\"" + request.deviceName().replace("\"", "\\\"") + "\",\"path\":\"" + path.replace("\\", "/") + "\"}",
                "{}"
        );

        String rawToken = "aifp_pc_" + UUID.randomUUID().toString().replace("-", "");
        String tokenHash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            tokenHash = HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            tokenHash = rawToken;
        }

        PcCompanionDeviceEntity device = new PcCompanionDeviceEntity(
                UUID.randomUUID(),
                principal.userId(),
                request.deviceName().trim(),
                request.osType().trim().toUpperCase(),
                tokenHash,
                path
        );

        PcCompanionDeviceEntity saved = deviceRepository.save(device);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                new PairDeviceResponse(
                        saved.getId(),
                        saved.getDeviceName(),
                        saved.getOsType(),
                        saved.getSandboxFolderPath(),
                        rawToken,
                        saved.getStatus()
                )
        ));
    }

    @PatchMapping("/devices/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<PcCompanionDeviceEntity>> updateStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @RequestParam String status
    ) {
        PcCompanionDeviceEntity device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        if (!device.getUserId().equals(principal.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: tenant isolation violation");
        }

        device.setStatus(status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(deviceRepository.save(device)));
    }

    @DeleteMapping("/devices/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> revokeDevice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        PcCompanionDeviceEntity device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        if (!device.getUserId().equals(principal.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: tenant isolation violation");
        }

        deviceRepository.delete(device);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

