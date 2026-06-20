package com.mt.expense.app.application.port.out;

import com.mt.expense.app.application.exception.UserAlreadyExistsException;
import com.mt.expense.app.infrastructure.web.dto.request.CreateUserRequest;
import com.mt.expense.app.infrastructure.web.dto.response.LoginResponse;

/** Outbound port for Keycloak authentication operations. */
public interface KeycloakAuthPort {

    /**
     * Authenticates a user with email and password via Keycloak.
     *
     * @param email user's email
     * @param password user's password
     * @return login response containing tokens
     * @throws AuthenticationException if credentials are invalid
     */
    LoginResponse authenticate(String email, String password);

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return new login response with fresh tokens
     */
    LoginResponse refresh(String refreshToken);

    /**
     * Creates a new user in Keycloak and returns a login response with valid tokens.
     *
     * @param request the user creation request
     * @return login response with valid tokens for the newly created user
     * @throws UserAlreadyExistsException if user with email already exists
     */
    LoginResponse createUser(CreateUserRequest request);
}
