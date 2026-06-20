package com.mt.expense.app.infrastructure.security;

import com.mt.expense.app.domain.vo.UserId;
import java.util.List;

/**
 * Framework-agnostic user principal extracted from JWT tokens. This value object flows through the
 * application layer via Commands.
 */
public record UserPrincipal(UserId userId, String email, List<String> roles) {
    public UserPrincipal {
        if (userId == null) throw new IllegalArgumentException("userId must not be null");
        if (email == null) throw new IllegalArgumentException("email must not be null");
        if (roles == null) roles = List.of();
    }

    /** Checks if this principal has the specified role. */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /** Checks if this principal has the ROLE_ADMIN role. */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /** Checks if this principal has the ROLE_USER role. */
    public boolean isUser() {
        return hasRole("ROLE_USER");
    }
}
