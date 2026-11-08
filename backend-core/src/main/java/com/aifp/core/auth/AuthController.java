package com.aifp.core.auth;

import com.aifp.core.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.AuthTokenResponse>> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.AuthTokenResponse>> login(
            @Valid @RequestBody AuthDtos.LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDtos.AuthTokenResponse>> refresh(
            @Valid @RequestBody AuthDtos.RefreshRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(authService.rotateRefreshToken(request.refreshToken())));
    }
}

