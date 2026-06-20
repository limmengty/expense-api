package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Request DTO for adding a member to a group. */
public record AddMemberRequest(@NotNull(message = "userId is required") UUID userId) {}
