package com.mt.expense.app.infrastructure.web.dto.response;

import java.math.BigDecimal;

public record SpendingPeriodResponse(String period, BigDecimal amount, int expenseCount) {}
