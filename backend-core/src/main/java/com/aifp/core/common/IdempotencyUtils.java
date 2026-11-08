package com.aifp.core.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

public final class IdempotencyUtils {

    private IdempotencyUtils() {
    }

    public static String computeExecutionIdempotencyKey(UUID taskId, Instant scheduledSlotUtc) {
        String raw = taskId.toString() + ":" + scheduledSlotUtc.toString();
        return sha256Hex(raw);
    }

    public static String computeContentFingerprint(String canonicalUrl, String title) {
        String normalizedUrl = (canonicalUrl == null ? "" : canonicalUrl.trim().toLowerCase(Locale.ROOT));
        String normalizedTitle = (title == null ? "" : title.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " "));
        return sha256Hex(normalizedUrl + "|" + normalizedTitle);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}

