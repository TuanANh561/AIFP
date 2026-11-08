package com.aifp.core.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class HmacSignatureSigner {

    private static final String HMAC_ALGO = "HmacSHA256";
    private final byte[] secretBytes;

    public HmacSignatureSigner(@Value("${aifp.security.internal-shared-secret}") String secret) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String signInternalRequest(long timestampSeconds, UUID userId, String rawBody) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secretBytes, HMAC_ALGO));
            String canonical = timestampSeconds + "." + userId + "." + (rawBody == null ? "" : rawBody);
            byte[] digest = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256 signature", e);
        }
    }

    public boolean verifyInternalRequest(long timestampSeconds, UUID userId, String rawBody, String expectedHex) {
        if (expectedHex == null || expectedHex.isBlank()) {
            return false;
        }
        String computed = signInternalRequest(timestampSeconds, userId, rawBody);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                expectedHex.getBytes(StandardCharsets.UTF_8)
        );
    }
}

