package com.mt.expense.app.application.exception;

import com.mt.expense.app.domain.vo.ExpenseId;

/** Thrown when attempting to modify or settle an expense that is already settled. */
public final class AlreadySettledException extends BusinessException {

    private final ExpenseId expenseId;

    public AlreadySettledException(ExpenseId expenseId) {
        super("Expense already settled: " + expenseId, "ALREADY_SETTLED");
        this.expenseId = expenseId;
    }

    public ExpenseId expenseId() {
        return expenseId;
    }
}
