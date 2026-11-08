package com.aifp.core.writing;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.tool.ToolPermissionGateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/writing")
public class AiWritingController {

    private final WritingDraftRepository writingRepository;
    private final ToolPermissionGateService toolPermissionGateService;

    public AiWritingController(
            WritingDraftRepository writingRepository,
            ToolPermissionGateService toolPermissionGateService
    ) {
        this.writingRepository = writingRepository;
        this.toolPermissionGateService = toolPermissionGateService;
    }

    public record CreateDraftRequest(
            @NotBlank String title,
            String docType, // EMAIL, REPORT, MEMO
            String tone,    // CONCISE, PROFESSIONAL, EXECUTIVE
            @NotBlank String promptBrief
    ) {}

    @GetMapping("/drafts")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<WritingDraftEntity>>> listDrafts(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                writingRepository.findByUserIdOrderByCreatedAtDesc(principal.userId())
        ));
    }

    @PostMapping("/draft")
    @Transactional
    public ResponseEntity<ApiResponse<WritingDraftEntity>> generateDraft(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateDraftRequest request
    ) {
        String normalizedType = request.docType() != null && !request.docType().isBlank()
                ? request.docType().trim().toUpperCase()
                : "EMAIL";
        String normalizedTone = request.tone() != null && !request.tone().isBlank()
                ? request.tone().trim().toUpperCase()
                : "CONCISE";

        // Evaluate Level 2 tool permission (writing_draft_generate)
        toolPermissionGateService.evaluateToolCall(
                principal.userId(),
                null,
                "writing_draft_generate",
                "{\"docType\":\"" + normalizedType + "\",\"tone\":\"" + normalizedTone + "\"}",
                "{}"
        );

        String brief = request.promptBrief().trim();
        String markdown = buildStructuredDraft(request.title().trim(), normalizedType, normalizedTone, brief);

        WritingDraftEntity entity = new WritingDraftEntity(
                UUID.randomUUID(),
                principal.userId(),
                request.title().trim(),
                normalizedType,
                normalizedTone,
                brief,
                markdown
        );
        return ResponseEntity.ok(ApiResponse.ok(writingRepository.save(entity)));
    }

    private String buildStructuredDraft(String title, String docType, String tone, String brief) {
        return switch (docType) {
            case "REPORT" -> "## Báo cáo điều hành: " + title + "\n"
                    + "**Phong cách**: `" + tone + "`\n\n"
                    + "### 1. Tóm tắt trọng tâm\n"
                    + "- " + brief + "\n\n"
                    + "### 2. Hành động tiếp theo\n"
                    + "- Rà soát chỉ số KPI và xác nhận mốc thời gian thực thi.\n"
                    + "- Lưu ý: Bản nháp nội bộ, chưa gửi ra ngoài.";
            case "MEMO" -> "## Bản ghi nhớ (Memo): " + title + "\n"
                    + "- **Mục tiêu**: " + brief + "\n"
                    + "- **Trạng thái**: Sẵn sàng rà soát (`" + tone + "`).";
            default -> "**Tiêu đề Email**: " + title + "\n\n"
                    + "Kính gửi Anh/Chị,\n\n"
                    + "Tôi xin phép trao đổi ngắn gọn về nội dung: **" + brief + "**.\n\n"
                    + "- **Đề xuất chính**: Xác nhận lịch trình và các đầu việc ưu tiên.\n"
                    + "- **Bước tiếp theo**: Mong nhận phản hồi từ Anh/Chị trước 17:00 hôm nay.\n\n"
                    + "Trân trọng,";
        };
    }
}

