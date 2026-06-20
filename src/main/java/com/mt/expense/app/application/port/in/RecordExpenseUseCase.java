package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.command.RecordExpenseCommand;
import com.mt.expense.app.domain.vo.ExpenseId;

/** Inbound Port (Primary) — Use Case for recording a new expense. */
public interface RecordExpenseUseCase {

    /**
     * Records a new expense in the system.
     *
     * @param command the command containing expense details
     * @return the ID of the newly created expense
     * @throws com.mt.expense.app.application.exception.SplitMismatchException if split amounts
     *     don't sum to total
     * @throws com.mt.expense.app.application.exception.NotGroupMemberException if payer is not a
     *     group member
     * @throws com.mt.expense.app.application.exception.GroupNotFoundException if group doesn't
     *     exist
     */
    ExpenseId recordExpense(RecordExpenseCommand command);
}
