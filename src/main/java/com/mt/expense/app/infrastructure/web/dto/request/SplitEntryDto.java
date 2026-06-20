package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/** DTO for a single split entry in an expense. */
public record SplitEntryDto(
        @NotNull(message = "userId is required") UUID userId,
        BigDecimal amount, // Required for EXACT strategy
        BigDecimal percentage // Required for PERCENTAGE strategy
        ) {}
