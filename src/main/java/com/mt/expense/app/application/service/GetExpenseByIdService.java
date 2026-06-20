package com.mt.expense.app.application.service;

import com.mt.expense.app.application.exception.ExpenseNotFoundException;
import com.mt.expense.app.application.port.in.GetExpenseByIdUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.vo.ExpenseId;

/** Implementation of GetExpenseByIdUseCase. */
@org.springframework.stereotype.Service
public final class GetExpenseByIdService implements GetExpenseByIdUseCase {

    private final ExpenseRepositoryPort expenseRepository;

    public GetExpenseByIdService(ExpenseRepositoryPort expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Expense getExpenseById(ExpenseId id) {
        return expenseRepository.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));
    }
}
