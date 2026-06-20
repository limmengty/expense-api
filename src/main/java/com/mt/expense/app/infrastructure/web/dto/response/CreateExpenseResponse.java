package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.vo.ExpenseId;
import java.time.Instant;
import java.util.UUID;

/** Response DTO for expense creation result. */
public record CreateExpenseResponse(UUID expenseId, Instant createdAt) {
    public static CreateExpenseResponse of(ExpenseId expenseId, Instant createdAt) {
        return new CreateExpenseResponse(expenseId.value(), createdAt);
    }
}
