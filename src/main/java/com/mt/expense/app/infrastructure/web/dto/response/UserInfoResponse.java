package com.mt.expense.app.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response DTO for current authenticated user info (from JWT token). */
public record UserInfoResponse(
        @JsonProperty("user_id") String userId,
        @JsonProperty("keycloak_id") String keycloakId,
        String email,
        String name,
        List<String> roles) {}
