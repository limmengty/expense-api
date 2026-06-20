package com.mt.expense.app.application.service;

import com.mt.expense.app.application.command.SettleExpenseCommand;
import com.mt.expense.app.application.exception.AlreadySettledException;
import com.mt.expense.app.application.exception.ExpenseNotFoundException;
import com.mt.expense.app.application.port.in.SettleExpenseUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.domain.model.Expense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of SettleExpenseUseCase. */
@org.springframework.stereotype.Service
public final class SettleExpenseService implements SettleExpenseUseCase {

    private static final Logger log = LoggerFactory.getLogger(SettleExpenseService.class);

    private final ExpenseRepositoryPort expenseRepository;

    public SettleExpenseService(ExpenseRepositoryPort expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public void settleExpense(SettleExpenseCommand command) {
        log.info("Settling expense {}", command.expenseId());

        Expense expense =
                expenseRepository
                        .findById(command.expenseId())
                        .orElseThrow(() -> new ExpenseNotFoundException(command.expenseId()));

        if (expense.isSettled()) {
            throw new AlreadySettledException(command.expenseId());
        }

        Expense settled = expense.settle();
        expenseRepository.save(settled);

        log.info("Expense {} settled successfully", command.expenseId());
    }
}
