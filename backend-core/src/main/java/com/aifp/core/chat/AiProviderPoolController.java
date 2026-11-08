package com.aifp.core.chat;

import com.aifp.core.common.AesGcmCryptoService;
import com.aifp.core.common.ApiResponse;
import com.aifp.core.notification.IntegrationEntity;
import com.aifp.core.notification.IntegrationRepository;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.security.SecurityAuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai-providers")
public class AiProviderPoolController {

    private static final Set<String> ALLOWED_PROVIDERS = Set.of(
            "GEMINI", "GROQ", "OPENROUTER", "OPENAI", "OLLAMA"
    );

    private final IntegrationRepository integrationRepository;
    private final AesGcmCryptoService cryptoService;
    private final SecurityAuditService auditService;

    public AiProviderPoolController(
            IntegrationRepository integrationRepository,
            AesGcmCryptoService cryptoService,
            SecurityAuditService auditService
    ) {
        this.integrationRepository = integrationRepository;
        this.cryptoService = cryptoService;
        this.auditService = auditService;
    }

    public record ProviderTierInfo(
            String providerCode,
            String providerName,
            String tierType, // FREE, HYBRID, PAID, OFFLINE
            List<String> models,
            String failoverOrder,
            String description
    ) {}

    public record ProviderPoolResponse(
            List<ProviderTierInfo> failoverPool,
            String activeStrategy,
            int totalProvidersConfigured
    ) {}

    public record RegisterApiKeyRequest(
            @NotBlank String provider, // GEMINI, GROQ, OPENROUTER, OPENAI
            @NotBlank String apiKey
    ) {}

    public record UserApiKeyView(
            UUID id,
            String provider,
            String maskedKey,
            String status
    ) {}

    @GetMapping("/pool")
    public ResponseEntity<ApiResponse<ProviderPoolResponse>> getProviderPool() {
        List<ProviderTierInfo> pool = List.of(
                new ProviderTierInfo(
                        "GEMINI",
                        "Google Gemini (Chính)",
                        "FREE_AND_PAID",
                        List.of("gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash", "gemini-2.0-flash-lite"),
                        "1 (Ưu tiên hàng đầu)",
                        "Hỗ trợ xoay vòng nhiều key. Khi 1 model hết quota hoặc 429, tự động trượt sang model Gemini kế tiếp."
                ),
                new ProviderTierInfo(
                        "GROQ",
                        "Groq Cloud LPUs",
                        "FREE",
                        List.of("llama-3.3-70b-versatile", "llama-3.1-8b-instant"),
                        "2 (Dự phòng tốc độ cao)",
                        "Kích hoạt tức thì khi toàn bộ key Gemini đạt trần phút/ngày. Hoàn toàn miễn phí với tốc độ > 300 t/s."
                ),
                new ProviderTierInfo(
                        "OPENROUTER",
                        "OpenRouter Hub",
                        "HYBRID_FREE_PAID",
                        List.of("gemini-2.0-flash-exp:free", "deepseek-chat:free", "llama-3.3-70b-instruct:free"),
                        "3 (Kho mô hình mở rộng)",
                        "Tổng hợp hàng trăm model. Tự động ưu tiên pool miễn phí (:free) trước khi sang tài khoản có phí."
                ),
                new ProviderTierInfo(
                        "OPENAI",
                        "OpenAI GPT",
                        "PAID",
                        List.of("gpt-4o-mini", "gpt-4o"),
                        "4 (Dự phòng thương mại)",
                        "Mô hình chuẩn mực ngành khi người dùng cấu hình khóa trả phí."
                ),
                new ProviderTierInfo(
                        "OLLAMA",
                        "Ollama Local Server",
                        "OFFLINE_ZERO_COST",
                        List.of("qwen2.5:7b", "llama3.2:3b"),
                        "5 (Dự phòng cứu cánh cuối)",
                        "Chạy offline trên máy chủ riêng khi mất kết nối mạng quốc tế hoặc hết sạch token bên ngoài."
                )
        );

        return ResponseEntity.ok(ApiResponse.ok(new ProviderPoolResponse(
                pool,
                "AUTOMATIC_FAILOVER_WITH_300S_COOLDOWN",
                pool.size()
        )));
    }

    @PostMapping("/keys")
    @Transactional
    public ResponseEntity<ApiResponse<UserApiKeyView>> registerUserApiKey(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody RegisterApiKeyRequest request
    ) {
        String normalizedProvider = request.provider().trim().toUpperCase();
        if (!ALLOWED_PROVIDERS.contains(normalizedProvider)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + normalizedProvider);
        }

        String rawKey = request.apiKey().trim();
        byte[] encrypted = cryptoService.encrypt(rawKey);

        IntegrationEntity entity = new IntegrationEntity(
                UUID.randomUUID(),
                principal.userId(),
                "AI_KEY_" + normalizedProvider,
                encrypted
        );
        IntegrationEntity saved = integrationRepository.save(entity);

        auditService.logEvent(
                principal.userId(),
                "USER_AI_KEY_REGISTERED",
                "INFO",
                "INTEGRATION",
                saved.getId().toString(),
                "{\"provider\":\"" + normalizedProvider + "\"}"
        );

        String masked = maskKey(rawKey);
        return ResponseEntity.ok(ApiResponse.ok(new UserApiKeyView(
                saved.getId(),
                normalizedProvider,
                masked,
                saved.getStatus()
        )));
    }

    @GetMapping("/my-keys")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<UserApiKeyView>>> listMyApiKeys(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<IntegrationEntity> integrations = integrationRepository.findByUserIdAndProviderStartingWithAndStatus(
                principal.userId(), "AI_KEY_", "ACTIVE"
        );

        List<UserApiKeyView> views = integrations.stream().map(it -> {
            String providerName = it.getProvider().replace("AI_KEY_", "");
            String rawDecrypted = cryptoService.decrypt(it.getEncryptedCredentials());
            return new UserApiKeyView(
                    it.getId(),
                    providerName,
                    maskKey(rawDecrypted),
                    it.getStatus()
            );
        }).toList();

        return ResponseEntity.ok(ApiResponse.ok(views));
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) return "configured";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}

