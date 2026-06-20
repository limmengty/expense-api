package com.mt.expense.app.application.port.out;

import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.vo.ExpenseId;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port (Secondary) — Repository interface for expense persistence. Implemented by
 * infrastructure adapters (e.g., JPA).
 */
public interface ExpenseRepositoryPort {

    /**
     * Saves an expense to the repository.
     *
     * @param expense the expense to save
     * @return the saved expense
     */
    Expense save(Expense expense);

    /**
     * Finds an expense by its ID.
     *
     * @param id the expense ID
     * @return an Optional containing the expense if found
     */
    Optional<Expense> findById(ExpenseId id);

    /**
     * Finds all expenses for a given group.
     *
     * @param groupId the group ID
     * @return list of expenses in the group
     */
    List<Expense> findAllByGroupId(GroupId groupId);

    /**
     * Finds all expenses matching the given specification with pagination.
     *
     * @param spec the JPA specification to filter by
     * @param pageable pagination parameters
     * @return a page of matching expenses
     */
    Page<Expense> findAll(
            org.springframework.data.jpa.domain.Specification<Expense> spec, Pageable pageable);

    /**
     * Finds all expenses for a given group.
     *
     * @param spec the JPA specification
     * @return list of expenses
     */
    List<Expense> findAllBySpec(org.springframework.data.jpa.domain.Specification<Expense> spec);

    /**
     * Finds all unsettled expenses for a given group and payer.
     *
     * @param groupId the group ID
     * @param payerId the payer ID
     * @return list of unsettled expenses
     */
    List<Expense> findUnsettledByGroupAndPayer(GroupId groupId, UserId payerId);

    /**
     * Deletes an expense by its ID.
     *
     * @param id the expense ID
     */
    void deleteById(ExpenseId id);
}
