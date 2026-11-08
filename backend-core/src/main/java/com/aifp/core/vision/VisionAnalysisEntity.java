package com.aifp.core.vision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vision_analysis_records")
public class VisionAnalysisEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;

    @Column(name = "analysis_mode", nullable = false, length = 64)
    private String analysisMode; // RECEIPT_OCR, DOCUMENT_OCR, PRODUCT_ATTRIBUTE, GENERAL_DESCRIPTION

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "extracted_data_json", columnDefinition = "TEXT")
    private String extractedDataJson;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "merchant_name", length = 160)
    private String merchantName;

    @Column(nullable = false, length = 32)
    private String status = "COMPLETED";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected VisionAnalysisEntity() {}

    public VisionAnalysisEntity(
            UUID id,
            UUID userId,
            String imageName,
            String analysisMode,
            String extractedText,
            String extractedDataJson,
            BigDecimal totalAmount,
            String merchantName
    ) {
        this.id = id;
        this.userId = userId;
        this.imageName = imageName;
        this.analysisMode = analysisMode;
        this.extractedText = extractedText;
        this.extractedDataJson = extractedDataJson;
        this.totalAmount = totalAmount;
        this.merchantName = merchantName;
        this.status = "COMPLETED";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getImageName() { return imageName; }
    public String getAnalysisMode() { return analysisMode; }
    public String getExtractedText() { return extractedText; }
    public String getExtractedDataJson() { return extractedDataJson; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getMerchantName() { return merchantName; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}

