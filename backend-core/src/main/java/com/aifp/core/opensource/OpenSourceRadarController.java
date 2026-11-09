package com.aifp.core.opensource;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/open-source")
public class OpenSourceRadarController {

    private final OpenSourceDiscoveryService discoveryService;

    public OpenSourceRadarController(OpenSourceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public record DiscoverRequest(
            @NotBlank String keywords,
            String goal,
            String email,
            boolean sendEmail
    ) {}

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserOpenSourceEntity>>> listUserItems(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "pinned", required = false) Boolean pinnedOnly
    ) {
        UUID userId = (principal != null) ? principal.userId() : UUID.fromString("00000000-0000-0000-0000-000000000001");
        List<UserOpenSourceEntity> items = discoveryService.getOrSeedUserItems(userId, pinnedOnly);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @PostMapping("/discover")
    public ResponseEntity<ApiResponse<List<UserOpenSourceEntity>>> discoverRepositories(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody DiscoverRequest request
    ) {
        UUID userId = (principal != null) ? principal.userId() : UUID.fromString("00000000-0000-0000-0000-000000000001");
        String targetEmail = (request.email() != null && !request.email().isBlank())
                ? request.email()
                : (principal != null ? principal.email() : null);

        List<UserOpenSourceEntity> updatedList = discoveryService.discoverAndPersist(
                userId,
                request.keywords(),
                request.goal(),
                request.sendEmail(),
                targetEmail
        );
        return ResponseEntity.ok(ApiResponse.ok(updatedList));
    }

    @PutMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<UserOpenSourceEntity>> togglePin(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID id
    ) {
        UUID userId = (principal != null) ? principal.userId() : UUID.fromString("00000000-0000-0000-0000-000000000001");
        UserOpenSourceEntity updated = discoveryService.togglePin(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteItem(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("id") UUID id
    ) {
        UUID userId = (principal != null) ? principal.userId() : UUID.fromString("00000000-0000-0000-0000-000000000001");
        boolean deleted = discoveryService.deleteItem(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "deleted", deleted,
                "id", id.toString()
        )));
    }

    @DeleteMapping("/clear-unpinned")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearUnpinned(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UUID userId = (principal != null) ? principal.userId() : UUID.fromString("00000000-0000-0000-0000-000000000001");
        int deletedCount = discoveryService.clearUnpinned(userId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "clearedCount", deletedCount,
                "status", "UNPINNED_ITEMS_CLEARED"
        )));
    }
}
