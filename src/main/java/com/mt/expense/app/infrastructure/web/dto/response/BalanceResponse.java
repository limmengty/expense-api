package com.mt.expense.app.infrastructure.web.dto.response;

import java.math.BigDecimal;

/**
 * Per-user balance DTO for the dashboard "Settle Up" panel. direction="owes" → the current user
 * owes this person (red, Pay button) direction="gets back" → this person owes the current user
 * (green, Settle button)
 */
public record BalanceResponse(
        String userId,
        String userName,
        String userAvatar,
        BigDecimal amount,
        String direction,
        String groupId) {}
