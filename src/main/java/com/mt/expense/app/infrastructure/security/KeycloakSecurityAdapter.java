package com.mt.expense.app.infrastructure.security;

import com.mt.expense.app.application.port.out.IdentityProviderPort;
import com.mt.expense.app.domain.vo.UserId;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Keycloak implementation of IdentityProviderPort. Extracts user information from JWT tokens issued
 * by Keycloak.
 */
@Component
public class KeycloakSecurityAdapter implements IdentityProviderPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakSecurityAdapter.class);

    @Override
    public UserPrincipal extractPrincipal(String token) {
        // This method is called from within a Spring Security filter chain
        // where the JWT is already validated and available in the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new SecurityException("No valid JWT found in security context");
        }

        return extractPrincipalFromJwt(jwt);
    }

    /** Extracts UserPrincipal from a validated Jwt token. */
    public UserPrincipal extractPrincipalFromJwt(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null) {
            // sub claim is missing from the Keycloak access token.
            // Fix: in Keycloak admin → Client Scopes → openid → Mappers
            // ensure a "subject" mapper exists with "Add to access token" = ON.
            throw new SecurityException(
                    "JWT is missing the 'sub' claim — configure the Keycloak 'expense-api' client "
                            + "to include 'sub' in access tokens (Client Scopes → openid → Mappers → subject)");
        }

        String email = jwt.getClaimAsString("email");
        List<String> roles = extractRoles(jwt);

        log.debug("Extracted principal: subject={}, email={}, roles={}", subject, email, roles);

        return new UserPrincipal(
                UserId.of(subject), email != null ? email : subject + "@unknown", roles);
    }

    @Override
    public boolean hasRole(UserPrincipal principal, String role) {
        return principal.hasRole(role);
    }

    @Override
    public List<String> getRoles(UserPrincipal principal) {
        return principal.roles();
    }

    /**
     * Extracts roles from Keycloak JWT. Keycloak stores roles in realm_access.roles for realm-level
     * roles.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        try {
            // Try Keycloak's realm_access structure first
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                return roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
                        .toList();
            }

            // Fallback: try resource_access for client-specific roles
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                // Could extract client-specific roles here if needed
            }

            // Fallback: check for simple roles claim
            List<String> simpleRoles = jwt.getClaimAsStringList("roles");
            if (simpleRoles != null) {
                return simpleRoles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
                        .toList();
            }

            return List.of("ROLE_USER"); // Default role
        } catch (Exception e) {
            log.warn("Failed to extract roles from JWT: {}", e.getMessage());
            return List.of("ROLE_USER");
        }
    }
}
