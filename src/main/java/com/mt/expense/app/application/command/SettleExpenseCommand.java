package com.mt.expense.app.application.command;

import com.mt.expense.app.domain.vo.ExpenseId;
import com.mt.expense.app.infrastructure.security.UserPrincipal;

/** Command object for settling an expense. */
public record SettleExpenseCommand(ExpenseId expenseId, UserPrincipal requestedBy) {
    public SettleExpenseCommand {
        if (expenseId == null) throw new IllegalArgumentException("expenseId must not be null");
        if (requestedBy == null) throw new IllegalArgumentException("requestedBy must not be null");
    }
}
