package com.mt.expense.app.application.service;

import com.mt.expense.app.application.command.DeleteExpenseCommand;
import com.mt.expense.app.application.exception.ExpenseNotFoundException;
import com.mt.expense.app.application.port.in.DeleteExpenseUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.application.port.out.IdentityProviderPort;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.infrastructure.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of DeleteExpenseUseCase. */
@org.springframework.stereotype.Service
public final class DeleteExpenseService implements DeleteExpenseUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteExpenseService.class);

    private final ExpenseRepositoryPort expenseRepository;
    private final IdentityProviderPort identityProvider;

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    public DeleteExpenseService(
            ExpenseRepositoryPort expenseRepository, IdentityProviderPort identityProvider) {
        this.expenseRepository = expenseRepository;
        this.identityProvider = identityProvider;
    }

    @Override
    public void deleteExpense(DeleteExpenseCommand command) {
        log.info(
                "Deleting expense {} by user {}",
                command.expenseId(),
                command.requestedBy().userId());

        Expense expense =
                expenseRepository
                        .findById(command.expenseId())
                        .orElseThrow(() -> new ExpenseNotFoundException(command.expenseId()));

        UserPrincipal principal = command.requestedBy();
        boolean isAdmin = identityProvider.hasRole(principal, ROLE_ADMIN);
        boolean isOwner = expense.payerId().value().equals(principal.userId().value());

        if (!isAdmin && !isOwner) {
            throw new SecurityException(
                    "Only the expense owner or an admin can delete this expense");
        }

        expenseRepository.deleteById(command.expenseId());
        log.info("Expense {} deleted successfully", command.expenseId());
    }
}
