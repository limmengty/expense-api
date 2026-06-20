package com.mt.expense.app.infrastructure.web.dto.response;

import java.util.UUID;

/** Response DTO for user summary (used in transfers). */
public record UserSummary(UUID userId, String name) {}
