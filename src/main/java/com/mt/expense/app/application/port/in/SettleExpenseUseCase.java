package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.command.SettleExpenseCommand;
import com.mt.expense.app.application.exception.AlreadySettledException;
import com.mt.expense.app.application.exception.ExpenseNotFoundException;

/** Inbound Port (Primary) — Use Case for settling an expense. */
public interface SettleExpenseUseCase {

    /**
     * Marks an expense as settled.
     *
     * @param command the command containing the expense ID to settle
     * @throws ExpenseNotFoundException if the expense doesn't exist
     * @throws AlreadySettledException if the expense is already settled
     */
    void settleExpense(SettleExpenseCommand command);
}
