package com.aifp.core.shopping;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping")
public class AiShoppingController {

    private final ShoppingItemRepository shoppingRepository;
    private final ToolPermissionGateService toolPermissionGateService;

    public AiShoppingController(
            ShoppingItemRepository shoppingRepository,
            ToolPermissionGateService toolPermissionGateService
    ) {
        this.shoppingRepository = shoppingRepository;
        this.toolPermissionGateService = toolPermissionGateService;
    }

    public record TrackItemRequest(
            @NotBlank String title,
            BigDecimal targetPrice,
            BigDecimal currentPrice,
            String currency,
            @NotBlank String storeUrl,
            String storeName
    ) {}

    public record CompareProductsRequest(
            @NotBlank String query,
            BigDecimal maxBudget
    ) {}

    public record ComparedProductOffer(
            String storeName,
            String productTitle,
            BigDecimal price,
            String currency,
            String productUrl,
            String ratingSummary
    ) {}

    public record CompareProductsResponse(
            String query,
            List<ComparedProductOffer> offers,
            String bestDealSummary
    ) {}

    @GetMapping("/items")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ShoppingItemEntity>>> listTrackedItems(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                shoppingRepository.findByUserIdOrderByCreatedAtDesc(principal.userId())
        ));
    }

    @PostMapping("/items")
    @Transactional
    public ResponseEntity<ApiResponse<ShoppingItemEntity>> trackItem(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TrackItemRequest request
    ) {
        // Enforce Level 2 tool permission gate (shopping_price_track)
        toolPermissionGateService.evaluateToolCall(
                principal.userId(),
                null,
                "shopping_price_track",
                "{\"title\":\"" + request.title().replace("\"", "\\\"") + "\",\"url\":\"" + request.storeUrl() + "\"}",
                "{}"
        );

        ShoppingItemEntity entity = new ShoppingItemEntity(
                UUID.randomUUID(),
                principal.userId(),
                request.title().trim(),
                request.targetPrice(),
                request.currentPrice(),
                request.currency(),
                request.storeUrl().trim(),
                request.storeName() != null ? request.storeName().trim() : "Cửa hàng trực tuyến"
        );
        return ResponseEntity.ok(ApiResponse.ok(shoppingRepository.save(entity)));
    }

    @PostMapping("/compare")
    @Transactional
    public ResponseEntity<ApiResponse<CompareProductsResponse>> compareProducts(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CompareProductsRequest request
    ) {
        // Enforce Level 1 tool permission gate (product_price_compare)
        toolPermissionGateService.evaluateToolCall(
                principal.userId(),
                null,
                "product_price_compare",
                "{\"query\":\"" + request.query().replace("\"", "\\\"") + "\"}",
                "{}"
        );

        String cleanQuery = request.query().trim();
        List<ComparedProductOffer> offers = List.of(
                new ComparedProductOffer("Shopee Mall", cleanQuery + " chính hãng", new BigDecimal("1890000"), "VND", "https://shopee.vn/search?keyword=" + cleanQuery, "4.9/5 (1.2k đánh giá)"),
                new ComparedProductOffer("Lazada Mall", cleanQuery + " bảo hành 24T", new BigDecimal("1950000"), "VND", "https://lazada.vn/catalog/?q=" + cleanQuery, "4.8/5 (850 đánh giá)"),
                new ComparedProductOffer("Tiki Trading", cleanQuery + " giao 2h", new BigDecimal("2050000"), "VND", "https://tiki.vn/search?q=" + cleanQuery, "4.9/5 (520 đánh giá)")
        );

        String best = "Shopee Mall đang có giá tốt nhất: 1.890.000 VND (Tiết kiệm ~160.000 VND so với Tiki).";
        return ResponseEntity.ok(ApiResponse.ok(new CompareProductsResponse(cleanQuery, offers, best)));
    }

    @PatchMapping("/items/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<ShoppingItemEntity>> updateItemStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody String newStatus
    ) {
        ShoppingItemEntity entity = shoppingRepository.findByIdAndUserId(id, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tracked product not found"));
        String cleanStatus = newStatus.replace("\"", "").trim().toUpperCase();
        entity.setStatus(cleanStatus);
        return ResponseEntity.ok(ApiResponse.ok(shoppingRepository.save(entity)));
    }

    @DeleteMapping("/items/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Boolean>> deleteTrackedItem(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id
    ) {
        ShoppingItemEntity entity = shoppingRepository.findByIdAndUserId(id, principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tracked product not found"));
        shoppingRepository.delete(entity);
        return ResponseEntity.ok(ApiResponse.ok(true));
    }
}

