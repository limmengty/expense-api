package com.mt.expense.app.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Login response containing tokens from Keycloak. */
public record LoginResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("refresh_expires_in") int refreshExpiresIn,
        @JsonProperty("scope") String scope,
        String email,
        String username,
        @JsonProperty("user_id") String userId) {}
