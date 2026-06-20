package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.exception.ExpenseNotFoundException;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.vo.ExpenseId;

/** Inbound Port (Primary) — Use Case for retrieving a single expense by ID. */
public interface GetExpenseByIdUseCase {

    /**
     * Retrieves an expense by its ID.
     *
     * @param id the expense ID
     * @return the expense
     * @throws ExpenseNotFoundException if the expense doesn't exist
     */
    Expense getExpenseById(ExpenseId id);
}
