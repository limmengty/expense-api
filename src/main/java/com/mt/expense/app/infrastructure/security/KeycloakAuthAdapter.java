package com.mt.expense.app.infrastructure.security;

import com.mt.expense.app.application.exception.AuthenticationException;
import com.mt.expense.app.application.port.out.KeycloakAuthPort;
import com.mt.expense.app.application.port.out.KeycloakTokenService;
import com.mt.expense.app.application.port.out.UserRepositoryPort;
import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.infrastructure.keycloak.KeycloakAdminService;
import com.mt.expense.app.infrastructure.web.dto.request.CreateUserRequest;
import com.mt.expense.app.infrastructure.web.dto.response.LoginResponse;
import com.mt.expense.app.infrastructure.web.dto.response.UserResponse;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing KeycloakAuthPort. Delegates token operations to KeycloakTokenService
 * (Feign-based). Handles user creation in both Keycloak and local DB.
 */
@Component
public class KeycloakAuthAdapter implements KeycloakAuthPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthAdapter.class);

    private final KeycloakAdminService keycloakAdminService;
    private final KeycloakTokenService tokenService;
    private final UserRepositoryPort userRepository;

    public KeycloakAuthAdapter(
            KeycloakAdminService keycloakAdminService,
            KeycloakTokenService tokenService,
            UserRepositoryPort userRepository) {
        this.keycloakAdminService = keycloakAdminService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public LoginResponse authenticate(String email, String password) {
        log.debug("Authenticating user: {}", email);
        try {
            Map<String, Object> tokens = tokenService.login(email, password);
            return toLoginResponse(tokens, email);
        } catch (Exception e) {
            log.warn("Authentication failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid credentials");
        }
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        log.debug("Refreshing token");
        try {
            Map<String, Object> tokens = tokenService.refresh(refreshToken);
            return toLoginResponse(tokens, null);
        } catch (Exception e) {
            throw new AuthenticationException("Invalid refresh token");
        }
    }

    @Override
    public LoginResponse createUser(CreateUserRequest request) {
        log.info("Creating user: {} in both Keycloak and local DB", request.username());

        String keycloakId;
        User savedUser;

        try {
            // Step 1: Create user in Keycloak
            keycloakId =
                    keycloakAdminService.createUser(
                            request.email(),
                            request.firstName() != null ? request.firstName() : request.username(),
                            request.lastName(),
                            request.password(),
                            UUID.randomUUID());
            log.info("Created user in Keycloak with ID: {}", keycloakId);

            // Step 2: Check if user already exists in local DB
            if (userRepository.existsByEmail(request.email())) {
                log.warn("User already exists in DB with email: {}", request.email());
                return authenticate(request.email(), request.password());
            }

            // Step 3: Create user in local DB
            UUID localUserId = UUID.randomUUID();
            Set<String> roles =
                    request.roles() != null
                            ? new HashSet<>(request.roles())
                            : new HashSet<>(Set.of("ROLE_USER"));

            User user =
                    User.reconstitute(
                            com.mt.expense.app.domain.vo.UserId.of(localUserId),
                            UUID.fromString(keycloakId),
                            request.email(),
                            request.username(),
                            roles,
                            true,
                            false,
                            java.time.Instant.now(),
                            java.time.Instant.now());

            savedUser = userRepository.save(user);
            log.info("Created user in local DB with ID: {}", savedUser.userId());

        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            throw new RuntimeException("User creation failed: " + e.getMessage(), e);
        }

        // Step 4: Return login response with valid tokens so frontend is immediately authenticated
        return authenticate(request.email(), request.password());
    }

    /** Verifies that a user exists in BOTH Keycloak AND local database. */
    public boolean verifyUserExistsInBothSystems(String keycloakId) {
        boolean existsInDb = userRepository.existsByKeycloakId(UUID.fromString(keycloakId));
        boolean existsInKeycloak = keycloakAdminService.userExists(keycloakId);
        log.debug(
                "User verification - KeycloakId: {}, InDB: {}, InKeycloak: {}",
                keycloakId,
                existsInDb,
                existsInKeycloak);
        return existsInDb && existsInKeycloak;
    }

    /** Syncs user from Keycloak to local DB if they exist in Keycloak but not in DB. */
    public User syncUserFromKeycloak(String keycloakId, String email, String name) {
        UUID kcId = UUID.fromString(keycloakId);
        if (userRepository.existsByKeycloakId(kcId)) {
            return userRepository.findByKeycloakId(kcId).orElseThrow();
        }
        User user =
                User.reconstitute(
                        com.mt.expense.app.domain.vo.UserId.random(),
                        kcId,
                        email,
                        name,
                        Set.of("ROLE_USER"),
                        true,
                        false,
                        java.time.Instant.now(),
                        java.time.Instant.now());
        User saved = userRepository.save(user);
        log.info("Synced user from Keycloak to DB: {}", saved.userId());
        return saved;
    }

    /** Gets user from Keycloak and syncs to local DB if needed. */
    public Optional<User> getOrSyncUser(String keycloakId, String email, String name) {
        UUID kcId = UUID.fromString(keycloakId);
        Optional<User> existing = userRepository.findByKeycloakId(kcId);
        if (existing.isPresent()) {
            return existing;
        }
        return Optional.of(syncUserFromKeycloak(keycloakId, email, name));
    }

    private LoginResponse toLoginResponse(Map<String, Object> tokens, String email) {
        String accessToken = (String) tokens.getOrDefault("access_token", "");
        String refreshToken = (String) tokens.getOrDefault("refresh_token", "");
        String tokenType = (String) tokens.getOrDefault("token_type", "Bearer");
        Integer expiresIn =
                tokens.get("expires_in") != null
                        ? ((Number) tokens.get("expires_in")).intValue()
                        : 300;
        Integer refreshExpiresIn =
                tokens.get("refresh_expires_in") != null
                        ? ((Number) tokens.get("refresh_expires_in")).intValue()
                        : 1800;
        String scope = (String) tokens.getOrDefault("scope", "");

        // sub is in the JWT payload, not the token response — call userinfo to get it reliably
        String userId = "";
        String resolvedEmail = email;
        String username = email;
        try {
            Map<String, Object> userInfo = tokenService.userInfo(accessToken);
            userId = (String) userInfo.getOrDefault("sub", "");
            if (resolvedEmail == null) resolvedEmail = (String) userInfo.getOrDefault("email", "");
            if (username == null)
                username = (String) userInfo.getOrDefault("preferred_username", "");
        } catch (Exception e) {
            log.warn("Could not fetch userinfo after token grant: {}", e.getMessage());
            resolvedEmail = resolvedEmail != null ? resolvedEmail : "";
            username = username != null ? username : "";
        }

        return new LoginResponse(
                accessToken,
                tokenType,
                expiresIn,
                refreshToken,
                refreshExpiresIn,
                scope,
                resolvedEmail,
                username,
                userId);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.userId().value().toString(),
                user.name(),
                user.email(),
                user.enabled(),
                user.verified(),
                new ArrayList<>(user.roles()));
    }
}
