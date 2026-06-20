package com.mt.expense.app.domain.model;

import static org.assertj.core.api.Assertions.*;

import com.mt.expense.app.domain.exception.SplitMismatchException;
import com.mt.expense.app.domain.vo.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for Expense domain entity. */
class ExpenseTest {

    private UserId alice = UserId.of(UUID.randomUUID());
    private UserId bob = UserId.of(UUID.randomUUID());
    private GroupId groupId = GroupId.random();

    @Nested
    @DisplayName("Expense creation")
    class ExpenseCreation {

        @Test
        @DisplayName("should create expense with valid splits")
        void shouldCreateExpenseWithValidSplits() {
            Money amount = Money.usd(new BigDecimal("90.00"));
            List<ExpenseSplit> splits =
                    List.of(
                            new ExpenseSplit(
                                    alice,
                                    Money.usd(new BigDecimal("45.00")),
                                    new BigDecimal("50.00")),
                            new ExpenseSplit(
                                    bob,
                                    Money.usd(new BigDecimal("45.00")),
                                    new BigDecimal("50.00")));

            Expense expense =
                    Expense.create(
                            groupId,
                            alice,
                            amount,
                            "Dinner",
                            new SplitStrategy.EqualSplit(2),
                            splits);

            assertThat(expense.id()).isNotNull();
            assertThat(expense.groupId()).isEqualTo(groupId);
            assertThat(expense.payerId()).isEqualTo(alice);
            assertThat(expense.amount()).isEqualTo(amount);
            assertThat(expense.description()).isEqualTo("Dinner");
            assertThat(expense.isSettled()).isFalse();
            assertThat(expense.splits()).hasSize(2);
        }

        @Test
        @DisplayName("should throw SplitMismatchException when splits don't sum to total")
        void shouldThrowWhenSplitsDontSumToTotal() {
            Money amount = Money.usd(new BigDecimal("100.00"));
            // Splits only add up to $50
            List<ExpenseSplit> splits =
                    List.of(
                            new ExpenseSplit(
                                    alice,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("25.00")),
                            new ExpenseSplit(
                                    bob,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("25.00")));

            assertThatThrownBy(
                            () ->
                                    Expense.create(
                                            groupId,
                                            alice,
                                            amount,
                                            "Dinner",
                                            new SplitStrategy.EqualSplit(2),
                                            splits))
                    .isInstanceOf(SplitMismatchException.class);
        }

        @Test
        @DisplayName("should create settled expense")
        void shouldCreateSettledExpense() {
            Money amount = Money.usd(new BigDecimal("50.00"));
            List<ExpenseSplit> splits =
                    List.of(
                            new ExpenseSplit(
                                    alice,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("50.00")),
                            new ExpenseSplit(
                                    bob,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("50.00")));

            Expense expense =
                    Expense.create(
                                    groupId,
                                    alice,
                                    amount,
                                    "Dinner",
                                    new SplitStrategy.EqualSplit(2),
                                    splits)
                            .settle();

            assertThat(expense.isSettled()).isTrue();
        }

        @Test
        @DisplayName("should be idempotent when settling already settled expense")
        void shouldBeIdempotentWhenSettlingAlreadySettledExpense() {
            Money amount = Money.usd(new BigDecimal("50.00"));
            List<ExpenseSplit> splits =
                    List.of(
                            new ExpenseSplit(
                                    alice,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("50.00")),
                            new ExpenseSplit(
                                    bob,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("50.00")));

            Expense expense =
                    Expense.create(
                            groupId,
                            alice,
                            amount,
                            "Dinner",
                            new SplitStrategy.EqualSplit(2),
                            splits);

            Expense firstSettle = expense.settle();
            Expense secondSettle = firstSettle.settle();

            assertThat(firstSettle.isSettled()).isTrue();
            assertThat(secondSettle.isSettled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Expense immutability")
    class ExpenseImmutability {

        @Test
        @DisplayName("should not allow modification after creation")
        void shouldNotAllowModificationAfterCreation() {
            Money amount = Money.usd(new BigDecimal("50.00"));
            List<ExpenseSplit> splits =
                    List.of(
                            new ExpenseSplit(
                                    alice,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("50.00")),
                            new ExpenseSplit(
                                    bob,
                                    Money.usd(new BigDecimal("25.00")),
                                    new BigDecimal("50.00")));

            Expense expense =
                    Expense.create(
                            groupId,
                            alice,
                            amount,
                            "Dinner",
                            new SplitStrategy.EqualSplit(2),
                            splits);

            // The expense object is immutable - any "modification" creates a new instance
            Expense settled = expense.settle();

            assertThat(expense.isSettled()).isFalse();
            assertThat(settled.isSettled()).isTrue();
            // Note: expense.equals() only compares by id, so same id means equal
            assertThat(expense.id()).isEqualTo(settled.id());
            assertThat(expense).isNotSameAs(settled);
        }
    }
}
