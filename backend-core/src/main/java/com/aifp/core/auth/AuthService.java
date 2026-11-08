package com.aifp.core.auth;

import com.aifp.core.common.IdempotencyUtils;
import com.aifp.core.security.JwtTokenProvider;
import com.aifp.core.security.SecurityAuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityAuditService auditService;
    private final long refreshTokenTtlSeconds;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            SecurityAuditService auditService,
            @Value("${aifp.security.refresh-token-ttl-seconds:604800}") long refreshTokenTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    @Transactional
    public AuthDtos.AuthTokenResponse register(AuthDtos.RegisterRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, normalizedEmail, passwordEncoder.encode(req.password()));
        userRepository.save(user);

        UserProfileEntity profile = new UserProfileEntity(userId, req.displayName().trim(), normalizedEmail);
        profileRepository.save(profile);

        auditService.logEvent(userId, "USER_REGISTERED", "INFO", "USER", userId.toString(), "{}");
        return issueTokenPair(user, profile.getDisplayName(), UUID.randomUUID());
    }

    @Transactional
    public AuthDtos.AuthTokenResponse login(AuthDtos.LoginRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase(Locale.ROOT);
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            auditService.logEvent(user.getId(), "LOGIN_FAILED", "WARN", "USER", user.getId().toString(), "{}");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String displayName = profileRepository.findById(user.getId())
                .map(UserProfileEntity::getDisplayName)
                .orElse(user.getEmail());

        auditService.logEvent(user.getId(), "LOGIN_SUCCESS", "INFO", "USER", user.getId().toString(), "{}");
        return issueTokenPair(user, displayName, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthDtos.AuthTokenResponse rotateRefreshToken(String rawRefreshToken) {
        String tokenHash = IdempotencyUtils.sha256Hex(rawRefreshToken);
        RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        UserEntity user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Reuse Detection: If an already-used token is presented again, revoke the entire token family!
        if (stored.isUsed() || stored.isRevoked()) {
            refreshTokenRepository.revokeFamily(user.getId(), stored.getFamilyId());
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
            auditService.logEvent(
                    user.getId(),
                    "SECURITY_TOKEN_REUSE_DETECTED",
                    "CRITICAL",
                    "REFRESH_TOKEN_FAMILY",
                    stored.getFamilyId().toString(),
                    "{\"action\":\"revoked_entire_family_and_bumped_token_version\"}"
            );
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token reuse detected; session family revoked");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        stored.setUsed(true);
        refreshTokenRepository.save(stored);

        String displayName = profileRepository.findById(user.getId())
                .map(UserProfileEntity::getDisplayName)
                .orElse(user.getEmail());

        return issueTokenPair(user, displayName, stored.getFamilyId());
    }

    private AuthDtos.AuthTokenResponse issueTokenPair(UserEntity user, String displayName, UUID familyId) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getTokenVersion());
        String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        String refreshHash = IdempotencyUtils.sha256Hex(rawRefreshToken);

        RefreshTokenEntity rt = new RefreshTokenEntity(
                UUID.randomUUID(),
                user.getId(),
                familyId,
                refreshHash,
                Instant.now().plusSeconds(refreshTokenTtlSeconds)
        );
        refreshTokenRepository.save(rt);

        return new AuthDtos.AuthTokenResponse(
                user.getId(),
                user.getEmail(),
                displayName,
                accessToken,
                rawRefreshToken,
                user.getDailyTokenQuota(),
                user.getDailyTokensUsed()
        );
    }
}

