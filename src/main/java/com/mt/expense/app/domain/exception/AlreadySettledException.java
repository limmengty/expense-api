package com.mt.expense.app.domain.exception;

import com.mt.expense.app.domain.vo.ExpenseId;

/** Thrown when attempting to modify or settle an expense that is already settled. */
public final class AlreadySettledException extends RuntimeException {

    private final ExpenseId expenseId;

    public AlreadySettledException(ExpenseId expenseId) {
        super("Expense already settled: " + expenseId);
        this.expenseId = expenseId;
    }

    public ExpenseId expenseId() {
        return expenseId;
    }
}
