package com.mt.expense.app.application.exception;

import com.mt.expense.app.domain.vo.ExpenseId;

/** Thrown when an expense cannot be found. */
public final class ExpenseNotFoundException extends BusinessException {

    private final ExpenseId expenseId;

    public ExpenseNotFoundException(ExpenseId expenseId) {
        super("Expense not found: " + expenseId, "EXPENSE_NOT_FOUND");
        this.expenseId = expenseId;
    }

    public ExpenseId expenseId() {
        return expenseId;
    }
}
