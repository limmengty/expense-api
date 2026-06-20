package com.mt.expense.app.application.port.out;

import com.mt.expense.app.infrastructure.security.UserPrincipal;
import java.util.List;

/**
 * Outbound Port (Secondary) — Interface for identity provider operations. Implemented by
 * KeycloakSecurityAdapter in infrastructure layer.
 */
public interface IdentityProviderPort {

    /**
     * Extracts the user principal from an authentication token.
     *
     * @param token the JWT token
     * @return the UserPrincipal containing user information
     */
    UserPrincipal extractPrincipal(String token);

    /**
     * Checks if the user has a specific role.
     *
     * @param principal the user principal
     * @param role the role to check
     * @return true if the user has the role
     */
    boolean hasRole(UserPrincipal principal, String role);

    /**
     * Gets the list of roles for a user.
     *
     * @param principal the user principal
     * @return list of role names
     */
    List<String> getRoles(UserPrincipal principal);
}
