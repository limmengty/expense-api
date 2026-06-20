package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.model.User;
import java.util.UUID;

/**
 * Lightweight user DTO for search results. Returns the Keycloak UUID as {@code id} because that is
 * the identifier used when adding members to groups (POST /api/v1/groups/{groupId}/members).
 */
public record UserSearchResult(UUID id, String name, String email) {

    public static UserSearchResult from(User user) {
        return new UserSearchResult(user.keycloakId(), user.name(), user.email());
    }
}
