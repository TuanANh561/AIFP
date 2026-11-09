package com.aifp.core.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 128) String password,
            @NotBlank @Size(min = 2, max = 80) String displayName
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}

    public record GoogleLoginRequest(
            @NotBlank String idToken
    ) {}

    public record AuthTokenResponse(
            UUID userId,
            String email,
            String displayName,
            String accessToken,
            String refreshToken,
            int dailyTokenQuota,
            int dailyTokensUsed
    ) {}
}

