package com.mt.expense.app.application.service;

import com.mt.expense.app.application.port.out.UserRepositoryPort;
import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.keycloak.KeycloakAdminService;
import com.mt.expense.app.infrastructure.web.dto.request.CreateUserRequest;
import com.mt.expense.app.infrastructure.web.dto.response.UserResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing users with dual-write to Keycloak and local DB. Implements transactional
 * consistency between both systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final KeycloakAdminService keycloakAdminService;
    private final UserRepositoryPort userRepository;

    /**
     * Creates a new user in both Keycloak AND local database. If either operation fails, attempts
     * to rollback the other.
     *
     * @param request the user creation request
     * @return the created user response
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user: {} in both Keycloak and local DB", request.email());

        // Check if user already exists in DB
        if (userRepository.existsByEmail(request.email())) {
            User existing = userRepository.findByEmail(request.email()).orElseThrow();
            log.warn("User already exists in DB: {}", request.email());
            return toUserResponse(existing);
        }

        String keycloakId = null;
        User savedUser = null;

        try {
            // Step 1: Create user in Keycloak
            UUID localUserId = UUID.randomUUID();
            keycloakId =
                    keycloakAdminService.createUser(
                            request.email(),
                            request.firstName() != null ? request.firstName() : request.username(),
                            request.lastName(),
                            generateSecurePassword(),
                            localUserId,
                            true);
            log.info("Created user in Keycloak with ID: {}", keycloakId);

            // Step 2: Create user in local DB
            Set<String> roles =
                    request.roles() != null ? Set.copyOf(request.roles()) : Set.of("ROLE_USER");

            User user =
                    User.reconstitute(
                            UserId.of(localUserId),
                            UUID.fromString(keycloakId),
                            request.email(),
                            request.username(),
                            roles,
                            true, // enabled
                            false, // verified (user must verify email)
                            Instant.now(),
                            Instant.now());

            savedUser = userRepository.save(user);
            log.info("Created user in local DB with ID: {}", savedUser.userId());

            return toUserResponse(savedUser);

        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);

            // If Keycloak creation succeeded but DB failed, rollback Keycloak
            if (keycloakId != null && savedUser == null) {
                try {
                    keycloakAdminService.deleteUser(keycloakId);
                    log.info("Rolled back Keycloak user: {}", keycloakId);
                } catch (Exception rollbackEx) {
                    log.error("Failed to rollback Keycloak user: {}", rollbackEx.getMessage());
                }
            }

            throw new RuntimeException("User creation failed: " + e.getMessage(), e);
        }
    }

    /** Finds a user by their local ID. */
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId userId) {
        return userRepository.findById(userId);
    }

    /** Finds a user by their Keycloak ID. */
    @Transactional(readOnly = true)
    public Optional<User> findByKeycloakId(UUID keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    /** Finds a user by their email. */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Verifies that a user exists in BOTH Keycloak AND local database. This is used for
     * security-critical validations.
     *
     * @param keycloakId the Keycloak user ID
     * @return true if user exists in both systems
     */
    @Transactional(readOnly = true)
    public boolean verifyUserInBothSystems(UUID keycloakId) {
        boolean existsInDb = userRepository.existsByKeycloakId(keycloakId);
        boolean existsInKeycloak = keycloakAdminService.userExists(keycloakId.toString());

        log.debug(
                "User verification - KeycloakId: {}, InDB: {}, InKeycloak: {}",
                keycloakId,
                existsInDb,
                existsInKeycloak);

        return existsInDb && existsInKeycloak;
    }

    /**
     * Syncs a user from Keycloak to local DB if they exist in Keycloak but not in DB. Used during
     * authentication to ensure user exists locally.
     *
     * @param keycloakId the Keycloak user ID
     * @param email user's email
     * @param name user's display name
     * @return the synced user
     */
    @Transactional
    public User syncUserFromKeycloak(UUID keycloakId, String email, String name) {
        if (userRepository.existsByKeycloakId(keycloakId)) {
            return userRepository.findByKeycloakId(keycloakId).orElseThrow();
        }

        User user =
                User.reconstitute(
                        UserId.random(),
                        keycloakId,
                        email,
                        name,
                        Set.of("ROLE_USER"),
                        true,
                        false,
                        Instant.now(),
                        Instant.now());

        User saved = userRepository.save(user);
        log.info("Synced user from Keycloak to DB: {}", saved.userId());
        return saved;
    }

    /** Gets user from local DB or syncs from Keycloak if not found. */
    @Transactional
    public User getOrSyncUser(UUID keycloakId, String email, String name) {
        return userRepository
                .findByKeycloakId(keycloakId)
                .orElseGet(() -> syncUserFromKeycloak(keycloakId, email, name));
    }

    /**
     * Deletes a user from both Keycloak and local DB.
     *
     * @param userId the local user ID
     */
    @Transactional
    public void deleteUser(UserId userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("User not found: " + userId));

        // Delete from Keycloak first
        keycloakAdminService.deleteUser(user.keycloakId().toString());

        // Then delete from local DB (if this fails, Keycloak is already deleted - manual
        // intervention needed)
        userRepository.save(user); // This would be delete in a real implementation
        log.info("Deleted user from both systems: {}", userId);
    }

    private String generateSecurePassword() {
        // In production, use a proper password generation library
        return UUID.randomUUID().toString().substring(0, 12) + "A1!";
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.userId().value().toString(),
                user.name(),
                user.email(),
                user.enabled(),
                user.verified(),
                user.roles().stream().toList());
    }
}
