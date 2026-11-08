package com.aifp.core.vision;

import com.aifp.core.common.AiOrchestratorClient;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vision")
public class AiVisionController {

    private final VisionAnalysisRepository visionRepository;
    private final ToolPermissionGateService toolPermissionGateService;
    private final AiOrchestratorClient aiOrchestratorClient;

    public AiVisionController(
            VisionAnalysisRepository visionRepository,
            ToolPermissionGateService toolPermissionGateService,
            AiOrchestratorClient aiOrchestratorClient
    ) {
        this.visionRepository = visionRepository;
        this.toolPermissionGateService = toolPermissionGateService;
        this.aiOrchestratorClient = aiOrchestratorClient;
    }

    public record VisionAnalyzeRequest(
            String imageName,
            @NotBlank String analysisMode, // RECEIPT_OCR, DOCUMENT_OCR, PRODUCT_ATTRIBUTE, GENERAL_DESCRIPTION
            String imageDataBase64,
            String prompt
    ) {}

    @GetMapping("/records")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<VisionAnalysisEntity>>> listUserRecords(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                visionRepository.findByUserIdOrderByCreatedAtDesc(principal.userId())
        ));
    }

    @PostMapping("/analyze")
    @Transactional
    public ResponseEntity<ApiResponse<VisionAnalysisEntity>> analyzeImage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody VisionAnalyzeRequest request
    ) {
        // Enforce Level 2 tool permission gate (vision_image_analyze)
        toolPermissionGateService.evaluateToolCall(
                principal.userId(),
                null,
                "vision_image_analyze",
                "{\"image_name\":\"" + (request.imageName() != null ? request.imageName().replace("\"", "\\\"") : "scan.png")
                        + "\",\"mode\":\"" + request.analysisMode() + "\"}",
                "{}"
        );

        AiOrchestratorClient.VisionAnalysisResult result = aiOrchestratorClient.analyzeVision(
                principal.userId(),
                request.imageName(),
                request.analysisMode(),
                request.imageDataBase64(),
                request.prompt()
        );

        VisionAnalysisEntity entity = new VisionAnalysisEntity(
                UUID.randomUUID(),
                principal.userId(),
                request.imageName() != null && !request.imageName().isBlank() ? request.imageName().trim() : "camera_capture.png",
                request.analysisMode().trim().toUpperCase(),
                result.extractedText(),
                result.extractedDataJson(),
                result.totalAmount(),
                result.merchantName()
        );

        VisionAnalysisEntity saved = visionRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    @DeleteMapping("/records/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        VisionAnalysisEntity entity = visionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));

        if (!entity.getUserId().equals(principal.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: tenant isolation violation");
        }

        visionRepository.delete(entity);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

