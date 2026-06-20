package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Login request DTO with email and password. */
public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
                String email,
        @NotBlank(message = "Password is required") String password) {}
