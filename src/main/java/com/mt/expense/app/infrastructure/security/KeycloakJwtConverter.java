package com.mt.expense.app.infrastructure.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unchecked")
public class KeycloakJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        extractRealmRoles(jwt, authorities);
        extractResourceRoles(jwt, authorities);
        return authorities;
    }

    private void extractRealmRoles(Jwt jwt, List<GrantedAuthority> authorities) {
        try {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    authorities.add(
                            new SimpleGrantedAuthority(
                                    role.startsWith("ROLE_")
                                            ? role
                                            : "ROLE_" + role.toUpperCase()));
                }
            }
        } catch (Exception e) {
            List<String> simpleRoles = jwt.getClaimAsStringList("roles");
            if (simpleRoles != null) {
                for (String role : simpleRoles) {
                    authorities.add(
                            new SimpleGrantedAuthority(
                                    role.startsWith("ROLE_")
                                            ? role
                                            : "ROLE_" + role.toUpperCase()));
                }
            }
        }
    }

    private void extractResourceRoles(Jwt jwt, List<GrantedAuthority> authorities) {
        try {
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                    Map<String, Object> resource = (Map<String, Object>) entry.getValue();
                    if (resource.containsKey("roles")) {
                        List<String> roles = (List<String>) resource.get("roles");
                        for (String role : roles) {
                            authorities.add(
                                    new SimpleGrantedAuthority(
                                            role.startsWith("ROLE_")
                                                    ? role
                                                    : "ROLE_" + role.toUpperCase()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
