package com.mt.expense.app.infrastructure.web.dto.response;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal totalGetBack,
        BigDecimal totalYouOwe,
        BigDecimal netBalance,
        int pendingSettlements) {}
