package com.aifp.core.security;

import java.util.UUID;

public record CustomUserPrincipal(
        UUID userId,
        String email,
        int tokenVersion
) {
}

