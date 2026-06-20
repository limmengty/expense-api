package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.command.DeleteExpenseCommand;
import com.mt.expense.app.application.exception.ExpenseNotFoundException;

/** Inbound Port (Primary) — Use Case for deleting an expense. */
public interface DeleteExpenseUseCase {

    /**
     * Deletes an expense. Only the owner or an admin can delete.
     *
     * @param command the command containing expense ID and requesting user
     * @throws ExpenseNotFoundException if the expense doesn't exist
     */
    void deleteExpense(DeleteExpenseCommand command);
}
