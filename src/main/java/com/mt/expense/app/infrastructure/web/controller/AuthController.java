package com.mt.expense.app.infrastructure.web.controller;

import com.mt.expense.app.application.port.out.KeycloakAuthPort;
import com.mt.expense.app.infrastructure.web.dto.request.CreateUserRequest;
import com.mt.expense.app.infrastructure.web.dto.request.LoginRequest;
import com.mt.expense.app.infrastructure.web.dto.response.LoginResponse;
import com.mt.expense.app.infrastructure.web.dto.response.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations. Provides login, register, token refresh, and
 * current user info endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final KeycloakAuthPort authPort;

    public AuthController(KeycloakAuthPort authPort) {
        this.authPort = authPort;
    }

    @Operation(summary = "Login", description = "Authenticate user with email and password")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.email());

        LoginResponse response = authPort.authenticate(request.email(), request.password());

        log.info("Login successful for user: {}", request.email());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Register",
            description = "Register a new user and return valid session tokens")
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("User registration request: {}", request.username());

        if (request.roles() == null || request.roles().isEmpty()) {
            request =
                    new CreateUserRequest(
                            request.username(),
                            request.email(),
                            request.password(),
                            request.firstName(),
                            request.lastName(),
                            List.of("ROLE_USER"));
        }

        LoginResponse response = authPort.createUser(request);

        log.info("User registered successfully: {}", request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request");

        LoginResponse response = authPort.refresh(request.refreshToken());

        log.debug("Token refresh successful");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Current user",
            description = "Returns the authenticated user's identity and roles")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("preferred_username");
        List<String> roles = jwt.getClaimAsStringList("roles");

        return ResponseEntity.ok(new UserInfoResponse(userId, userId, email, name, roles));
    }

    public record RefreshTokenRequest(String refreshToken) {}
}
