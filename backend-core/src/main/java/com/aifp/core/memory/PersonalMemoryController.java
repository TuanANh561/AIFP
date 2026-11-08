package com.aifp.core.memory;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memories")
public class PersonalMemoryController {

    private final PersonalMemoryService memoryService;

    public PersonalMemoryController(PersonalMemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public record CreateMemoryRequest(
            @NotBlank @Size(max = 48) String category,
            @Size(max = 120) String topicKey,
            @NotBlank @Size(max = 2000) String content,
            boolean candidate
    ) {}

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiMemoryEntity>>> listMemories(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(memoryService.listVisibleMemories(principal.userId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiMemoryEntity>> createMemory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateMemoryRequest request
    ) {
        AiMemoryEntity saved = memoryService.upsertMemory(
                principal.userId(),
                request.category(),
                request.topicKey(),
                request.content(),
                request.candidate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    @PostMapping("/{memoryId}/approve")
    public ResponseEntity<ApiResponse<AiMemoryEntity>> approveCandidate(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID memoryId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                memoryService.approveCandidateMemory(principal.userId(), memoryId)
        ));
    }

    @DeleteMapping("/{memoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteMemory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID memoryId
    ) {
        memoryService.deleteMemory(principal.userId(), memoryId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

