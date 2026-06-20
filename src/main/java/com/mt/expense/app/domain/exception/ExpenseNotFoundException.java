package com.mt.expense.app.domain.exception;

import com.mt.expense.app.domain.vo.ExpenseId;

/** Thrown when an expense cannot be found. */
public final class ExpenseNotFoundException extends RuntimeException {

    private final ExpenseId expenseId;

    public ExpenseNotFoundException(ExpenseId expenseId) {
        super("Expense not found: " + expenseId);
        this.expenseId = expenseId;
    }

    public ExpenseId expenseId() {
        return expenseId;
    }
}
