package com.mt.expense.app.domain.model;

import com.mt.expense.app.domain.vo.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Domain Entity representing a user. Links local user profile with Keycloak identity. */
public final class User {

    private final UserId userId;
    private final UUID keycloakId;
    private final String email;
    private final String name;
    private final Set<String> roles;
    private final boolean enabled;
    private final boolean verified;
    private final Instant createdAt;
    private final Instant updatedAt;

    public User(
            UserId userId,
            UUID keycloakId,
            String email,
            String name,
            Set<String> roles,
            boolean enabled,
            boolean verified,
            Instant createdAt,
            Instant updatedAt) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.keycloakId = Objects.requireNonNull(keycloakId, "keycloakId must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.roles = Set.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
        this.enabled = enabled;
        this.verified = verified;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public static User create(UUID keycloakId, String email, String name) {
        Instant now = Instant.now();
        return new User(
                UserId.random(),
                keycloakId,
                email,
                name,
                Set.of("ROLE_USER"),
                true,
                false,
                now,
                now);
    }

    public static User reconstitute(
            UserId userId,
            UUID keycloakId,
            String email,
            String name,
            Set<String> roles,
            boolean enabled,
            boolean verified,
            Instant createdAt,
            Instant updatedAt) {
        return new User(
                userId, keycloakId, email, name, roles, enabled, verified, createdAt, updatedAt);
    }

    public UserId userId() {
        return userId;
    }

    public UUID keycloakId() {
        return keycloakId;
    }

    public String email() {
        return email;
    }

    public String name() {
        return name;
    }

    public Set<String> roles() {
        return roles;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean verified() {
        return verified;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}
