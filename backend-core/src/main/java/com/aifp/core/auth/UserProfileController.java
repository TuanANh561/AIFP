package com.aifp.core.auth;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.security.SecurityAuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController {

    private static final Set<String> VALID_PERSONAS = Set.of(
            "STUDENT",
            "OFFICE_WORKER",
            "DEVELOPER",
            "BUSINESS",
            "SHOPPER",
            "CREATOR",
            "JOB_SEEKER",
            "GENERAL"
    );

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final SecurityAuditService auditService;

    public UserProfileController(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            SecurityAuditService auditService
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.auditService = auditService;
    }

    public record UserProfileView(
            UUID userId,
            String email,
            String displayName,
            String personaType,
            String timezone,
            boolean memoryEnabled,
            int dailyTokenQuota,
            int dailyTokensUsed,
            List<String> recommendedTools
    ) {}

    public record UpdateProfileRequest(
            String displayName,
            String personaType,
            String timezone,
            Boolean memoryEnabled
    ) {}

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<UserProfileView>> getMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserProfileEntity profile = profileRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return ResponseEntity.ok(ApiResponse.ok(toView(user, profile)));
    }

    @PatchMapping("/profile")
    @Transactional
    public ResponseEntity<ApiResponse<UserProfileView>> updateMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        UserEntity user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserProfileEntity profile = profileRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (request.displayName() != null && !request.displayName().isBlank()) {
            profile.setDisplayName(request.displayName().trim());
        }
        if (request.personaType() != null && !request.personaType().isBlank()) {
            String normalizedPersona = request.personaType().trim().toUpperCase();
            if (!VALID_PERSONAS.contains(normalizedPersona)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported personaType: " + normalizedPersona);
            }
            profile.setPersonaType(normalizedPersona);
        }
        if (request.timezone() != null && !request.timezone().isBlank()) {
            profile.setTimezone(request.timezone().trim());
        }
        if (request.memoryEnabled() != null) {
            profile.setMemoryEnabled(request.memoryEnabled());
        }
        profile.setUpdatedAt(Instant.now());
        profileRepository.save(profile);

        auditService.logEvent(
                principal.userId(),
                "USER_PROFILE_UPDATED",
                "INFO",
                "USER_PROFILE",
                principal.userId().toString(),
                "{\"personaType\":\"" + profile.getPersonaType() + "\"}"
        );
        return ResponseEntity.ok(ApiResponse.ok(toView(user, profile)));
    }

    private UserProfileView toView(UserEntity user, UserProfileEntity profile) {
        List<String> recommended = switch (profile.getPersonaType()) {
            case "STUDENT" -> List.of("doc_parse", "translate_text", "calendar_event_read", "searxng_search");
            case "OFFICE_WORKER" -> List.of("writing_draft_generate", "calendar_event_write", "email_send_self", "doc_parse");
            case "DEVELOPER" -> List.of("searxng_search", "rss_fetch", "webpage_extract", "translate_text");
            case "BUSINESS" -> List.of("searxng_search", "writing_draft_generate", "email_send_external", "calendar_event_write");
            case "JOB_SEEKER" -> List.of("searxng_search", "webpage_extract", "email_send_self", "writing_draft_generate");
            default -> List.of("searxng_search", "translate_text", "memory_candidate_save", "notification_send");
        };

        return new UserProfileView(
                user.getId(),
                user.getEmail(),
                profile.getDisplayName(),
                profile.getPersonaType(),
                profile.getTimezone(),
                profile.isMemoryEnabled(),
                user.getDailyTokenQuota(),
                user.getDailyTokensUsed(),
                recommended
        );
    }
}

