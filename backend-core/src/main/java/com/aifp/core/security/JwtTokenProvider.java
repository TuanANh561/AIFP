package com.aifp.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenTtlSeconds;

    public JwtTokenProvider(
            @Value("${aifp.security.jwt-secret}") String jwtSecret,
            @Value("${aifp.security.access-token-ttl-seconds:900}") long accessTokenTtlSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public String createAccessToken(UUID userId, String email, int tokenVersion) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("tv", tokenVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenTtlSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public CustomUserPrincipal parseAndValidate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.get("email", String.class);
        Integer tv = claims.get("tv", Integer.class);
        return new CustomUserPrincipal(userId, email, tv != null ? tv : 1);
    }
}

