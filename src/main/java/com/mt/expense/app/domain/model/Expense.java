package com.mt.expense.app.domain.model;

import com.mt.expense.app.domain.exception.SplitMismatchException;
import com.mt.expense.app.domain.vo.ExpenseId;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.SplitStrategy;
import com.mt.expense.app.domain.vo.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain Entity representing an expense within a group. Encapsulates all business invariants
 * including split validation.
 */
public final class Expense {

    private final ExpenseId id;
    private final GroupId groupId;
    private final UserId payerId;
    private final Money amount;
    private final String description;
    private final SplitStrategy splitStrategy;
    private final List<ExpenseSplit> splits;
    private final boolean settled;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Expense(
            ExpenseId id,
            GroupId groupId,
            UserId payerId,
            Money amount,
            String description,
            SplitStrategy splitStrategy,
            List<ExpenseSplit> splits,
            boolean settled,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.groupId = Objects.requireNonNull(groupId, "groupId must not be null");
        this.payerId = Objects.requireNonNull(payerId, "payerId must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.description = description;
        this.splitStrategy =
                Objects.requireNonNull(splitStrategy, "splitStrategy must not be null");
        this.splits = List.copyOf(splits); // immutable copy
        this.settled = settled;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        validateSplits();
    }

    /** Factory method to create a new unsettled expense. */
    public static Expense create(
            GroupId groupId,
            UserId payerId,
            Money amount,
            String description,
            SplitStrategy splitStrategy,
            List<ExpenseSplit> splits) {
        Instant now = Instant.now();
        return new Expense(
                ExpenseId.random(),
                groupId,
                payerId,
                amount,
                description,
                splitStrategy,
                splits,
                false,
                now,
                now);
    }

    /** Factory method to reconstruct an expense from persistence. */
    public static Expense reconstitute(
            ExpenseId id,
            GroupId groupId,
            UserId payerId,
            Money amount,
            String description,
            SplitStrategy splitStrategy,
            List<ExpenseSplit> splits,
            boolean settled,
            Instant createdAt,
            Instant updatedAt) {
        return new Expense(
                id,
                groupId,
                payerId,
                amount,
                description,
                splitStrategy,
                splits,
                settled,
                createdAt,
                updatedAt);
    }

    private void validateSplits() {
        Money sumOfSplits =
                splits.stream()
                        .map(ExpenseSplit::shareAmount)
                        .reduce(Money.usd(java.math.BigDecimal.ZERO), Money::add);

        if (!sumOfSplits.equals(amount)) {
            throw new SplitMismatchException(sumOfSplits, amount);
        }
    }

    /** Marks this expense as settled. */
    public Expense settle() {
        if (settled) {
            return this; // idempotent
        }
        return new Expense(
                id,
                groupId,
                payerId,
                amount,
                description,
                splitStrategy,
                splits,
                true,
                createdAt,
                Instant.now());
    }

    // Getters
    public ExpenseId id() {
        return id;
    }

    public GroupId groupId() {
        return groupId;
    }

    public UserId payerId() {
        return payerId;
    }

    public Money amount() {
        return amount;
    }

    public String description() {
        return description;
    }

    public SplitStrategy splitStrategy() {
        return splitStrategy;
    }

    public List<ExpenseSplit> splits() {
        return splits;
    }

    public boolean isSettled() {
        return settled;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return id.equals(expense.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Expense{id="
                + id
                + ", groupId="
                + groupId
                + ", payerId="
                + payerId
                + ", amount="
                + amount
                + ", description='"
                + description
                + '\''
                + ", settled="
                + settled
                + '}';
    }
}
