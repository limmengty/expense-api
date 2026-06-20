package com.mt.expense.app.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response DTO for created user. */
public record UserResponse(
        @JsonProperty("user_id") String userId,
        String username,
        String email,
        boolean enabled,
        @JsonProperty("email_verified") boolean emailVerified,
        List<String> roles) {}
