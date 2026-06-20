package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.query.ExpenseQuery;
import com.mt.expense.app.domain.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Inbound Port (Primary) — Use Case for querying expenses with filters. */
public interface QueryExpensesUseCase {

    /**
     * Queries expenses with dynamic filtering via JPA Specifications.
     *
     * @param query the query parameters for filtering
     * @param pageable pagination parameters
     * @return a page of matching expenses
     */
    Page<Expense> queryExpenses(ExpenseQuery query, Pageable pageable);
}
