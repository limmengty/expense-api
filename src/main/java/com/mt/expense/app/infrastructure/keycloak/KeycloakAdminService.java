package com.mt.expense.app.infrastructure.keycloak;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final Keycloak keycloak;
    private final KeycloakProperty keycloakProperty;

    /**
     * Creates a user in Keycloak with a verified, fully-configured account.
     *
     * <p>Design notes: - Credentials are NOT set inline in the UserRepresentation — some Keycloak
     * versions ignore the credentials field on creation. We always call resetPassword() explicitly.
     * - firstName/lastName are never set to empty string because Keycloak 26.x User Profile
     * validation marks empty required attributes as UPDATE_PROFILE during the Direct Grant login
     * check, overriding our finalizeUserSetup() clearing of requiredActions.
     */
    public String createUser(
            String email,
            String firstName,
            String lastName,
            String password,
            UUID keycloakId,
            boolean temporaryPassword) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(List.of());
        user.setAttributes(java.util.Map.of("user_id", List.of(keycloakId.toString())));

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName.trim());
        }

        UsersResource usersResource = keycloak.realm(keycloakProperty.getRealm()).users();
        var response = usersResource.create(user);

        if (response.getStatus() == 409) {
            List<UserRepresentation> existing = usersResource.searchByUsername(email, true);
            if (existing.isEmpty()) {
                throw new IllegalStateException("User conflict but not found: " + email);
            }
            String existingId = existing.get(0).getId();
            log.info("Keycloak user already exists: {} (ID: {})", email, existingId);
            resetPassword(existingId, password, temporaryPassword);
            finalizeUserSetup(existingId, usersResource);
            return existingId;
        }

        if (response.getLocation() == null) {
            throw new IllegalStateException(
                    "Keycloak user creation failed with status: " + response.getStatus());
        }

        String createdId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        log.info("Created Keycloak user: {} (ID: {})", email, createdId);

        // Password must be set via resetPassword() — inline credentials in the creation
        // payload are silently ignored in some Keycloak versions.
        resetPassword(createdId, password, temporaryPassword);
        finalizeUserSetup(createdId, usersResource);
        return createdId;
    }

    public String createUser(
            String email, String firstName, String lastName, String password, UUID keycloakId) {
        return createUser(email, firstName, lastName, password, keycloakId, false);
    }

    public void deleteUser(String keycloakUserId) {
        keycloak.realm(keycloakProperty.getRealm()).users().delete(keycloakUserId);
        log.info("Deleted Keycloak user: {}", keycloakUserId);
    }

    public void resetPassword(String keycloakUserId, String newPassword, boolean temporary) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(temporary);

        keycloak.realm(keycloakProperty.getRealm())
                .users()
                .get(keycloakUserId)
                .resetPassword(credential);
        log.info("Reset password for Keycloak user: {} (temporary={})", keycloakUserId, temporary);
    }

    public String findUserByEmail(String email) {
        List<UserRepresentation> users =
                keycloak.realm(keycloakProperty.getRealm()).users().searchByUsername(email, true);
        return users.isEmpty() ? null : users.get(0).getId();
    }

    public boolean userExists(String keycloakUserId) {
        try {
            keycloak.realm(keycloakProperty.getRealm())
                    .users()
                    .get(keycloakUserId)
                    .toRepresentation();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setUserEnabled(String keycloakUserId, boolean enabled) {
        UserResource user = keycloak.realm(keycloakProperty.getRealm()).users().get(keycloakUserId);
        UserRepresentation rep = user.toRepresentation();
        rep.setEnabled(enabled);
        user.update(rep);
        log.info("Updated Keycloak user {} enabled={}", keycloakUserId, enabled);
    }

    /**
     * Final step after any user create/update: guarantee emailVerified=true and no pending required
     * actions so the account passes Keycloak's Direct Grant login checks. Called for BOTH the 201
     * (new) and 409 (existing) paths so the guarantee is universal.
     */
    private void finalizeUserSetup(String keycloakId, UsersResource usersResource) {
        UserResource userResource = usersResource.get(keycloakId);
        UserRepresentation rep = userResource.toRepresentation();
        rep.setEmailVerified(true);
        rep.setRequiredActions(List.of());
        userResource.update(rep);
        log.info(
                "Finalized Keycloak user: {} (emailVerified=true, requiredActions cleared)",
                keycloakId);
    }
}
