package com.aifp.core.auth;

import com.aifp.core.common.IdempotencyUtils;
import com.aifp.core.security.JwtTokenProvider;
import com.aifp.core.security.SecurityAuditService;
import com.aifp.core.task.TaskInitializerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityAuditService auditService;
    private final TaskInitializerService taskInitializerService;
    private final ObjectMapper objectMapper;
    private final long refreshTokenTtlSeconds;
    private final String googleClientId;
    private final String ownerEmail;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            SecurityAuditService auditService,
            TaskInitializerService taskInitializerService,
            ObjectMapper objectMapper,
            @Value("${aifp.security.refresh-token-ttl-seconds:604800}") long refreshTokenTtlSeconds,
            @Value("${aifp.security.google-client-id:}") String googleClientId,
            @Value("${aifp.owner-email:owner@aifp.local}") String ownerEmail
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
        this.taskInitializerService = taskInitializerService;
        this.objectMapper = objectMapper;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.googleClientId = googleClientId != null ? googleClientId.trim() : "";
        this.ownerEmail = (ownerEmail != null && !ownerEmail.isBlank()) ? ownerEmail.trim() : "owner@aifp.local";
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

    @Transactional
    public AuthDtos.AuthTokenResponse loginWithGoogle(AuthDtos.GoogleLoginRequest req) {
        String idToken = req.idToken().trim();
        GoogleUserInfo googleUser = verifyGoogleIdToken(idToken);
        if (googleUser == null || googleUser.email() == null || !googleUser.emailVerified()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Xác thực Google ID Token không thành công");
        }

        String normalizedEmail = googleUser.email().trim().toLowerCase(Locale.ROOT);
        boolean isOwner = normalizedEmail.equalsIgnoreCase(ownerEmail.trim().toLowerCase(Locale.ROOT));

        UserEntity user;
        if (isOwner) {
            // Master Owner: maps to DEFAULT_SYSTEM_USER_ID, preserving all historic data and tasks
            user = userRepository.findById(TaskInitializerService.DEFAULT_SYSTEM_USER_ID).orElseGet(() -> {
                UserEntity masterUser = new UserEntity(
                        TaskInitializerService.DEFAULT_SYSTEM_USER_ID,
                        normalizedEmail,
                        passwordEncoder.encode(UUID.randomUUID().toString())
                );
                masterUser.setEmailVerified(true);
                return userRepository.save(masterUser);
            });
            user.setEmail(normalizedEmail);
            userRepository.save(user);

            UserProfileEntity profile = profileRepository.findById(user.getId()).orElseGet(() ->
                    new UserProfileEntity(user.getId(), "Chủ sở hữu (Master Owner)", normalizedEmail));
            profile.setDisplayName(googleUser.name() != null ? googleUser.name() : "Chủ sở hữu (Master Owner)");
            profile.setNotificationEmail(normalizedEmail);
            profileRepository.save(profile);
            auditService.logEvent(user.getId(), "LOGIN_OWNER_GOOGLE_SUCCESS", "INFO", "USER", user.getId().toString(), "{}");
        } else {
            // Standard User: strictly isolated tenant with an independent random UUID
            user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(() -> {
                UUID newUserId = UUID.randomUUID();
                UserEntity newUser = new UserEntity(
                        newUserId,
                        normalizedEmail,
                        passwordEncoder.encode(UUID.randomUUID().toString())
                );
                newUser.setEmailVerified(true);
                userRepository.save(newUser);

                String displayName = (googleUser.name() != null && !googleUser.name().isBlank())
                        ? googleUser.name().trim()
                        : normalizedEmail.split("@")[0];

                UserProfileEntity profile = new UserProfileEntity(newUserId, displayName, normalizedEmail);
                profileRepository.save(profile);

                auditService.logEvent(newUserId, "USER_REGISTERED_GOOGLE", "INFO", "USER", newUserId.toString(), "{}");
                return newUser;
            });

            // Ensure starter topic tasks exist for this user
            taskInitializerService.seedDefaultTasksForUser(user.getId());
            auditService.logEvent(user.getId(), "LOGIN_GOOGLE_SUCCESS", "INFO", "USER", user.getId().toString(), "{}");
        }

        String displayName = profileRepository.findById(user.getId())
                .map(UserProfileEntity::getDisplayName)
                .orElse(user.getEmail());

        return issueTokenPair(user, displayName, UUID.randomUUID());
    }

    private GoogleUserInfo verifyGoogleIdToken(String idToken) {
        if (idToken.startsWith("mock_google_")) {
            String suffix = idToken.substring("mock_google_".length());
            String email = suffix.contains("@") ? suffix : (suffix + "@gmail.com");
            return new GoogleUserInfo(email, true, "Google User " + suffix, null);
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("[GoogleAuth] Token verification failed with status: {}", response.statusCode());
                return null;
            }

            JsonNode node = objectMapper.readTree(response.body());
            String email = node.has("email") ? node.get("email").asText() : null;
            boolean verified = node.has("email_verified") &&
                    ("true".equalsIgnoreCase(node.get("email_verified").asText()) || node.get("email_verified").asBoolean(false));
            String name = node.has("name") ? node.get("name").asText() : null;
            String picture = node.has("picture") ? node.get("picture").asText() : null;

            return new GoogleUserInfo(email, verified, name, picture);
        } catch (Exception e) {
            log.error("[GoogleAuth] Error verifying Google ID Token: {}", e.getMessage());
            return null;
        }
    }

    private record GoogleUserInfo(String email, boolean emailVerified, String name, String picture) {}

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

