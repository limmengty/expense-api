package com.mt.expense.app.application.port.out;

import java.util.Map;

/** Outbound port for Keycloak token operations (login, refresh, logout). */
public interface KeycloakTokenService {

    /**
     * Authenticates a user with username and password via Keycloak.
     *
     * @param username user's email/username
     * @param password user's password
     * @return map containing access_token, refresh_token, expires_in, token_type
     */
    Map<String, Object> login(String username, String password);

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return map containing new access_token, refresh_token, expires_in, token_type
     */
    Map<String, Object> refresh(String refreshToken);

    /**
     * Logs out by invalidating the refresh token.
     *
     * @param refreshToken the refresh token to invalidate
     */
    void logout(String refreshToken);

    /**
     * Calls Keycloak userinfo endpoint to get user claims (including sub/UUID). Use after
     * login/refresh to obtain the user's UUID when it is missing from the access token.
     *
     * @param accessToken the access token (without "Bearer " prefix)
     * @return map of user claims including sub, email, preferred_username
     */
    Map<String, Object> userInfo(String accessToken);
}
