package com.mt.expense.app.domain.service;

import static org.assertj.core.api.Assertions.*;

import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.ExpenseSplit;
import com.mt.expense.app.domain.vo.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for DebtSimplificationService. Pure domain service tests with NO Spring context. */
class DebtSimplificationServiceTest {

    private DebtSimplificationService service;

    // Test users
    private UserId alice;
    private UserId bob;
    private UserId carol;

    @BeforeEach
    void setUp() {
        service = new DebtSimplificationService();
        alice = UserId.of(UUID.randomUUID());
        bob = UserId.of(UUID.randomUUID());
        carol = UserId.of(UUID.randomUUID());
    }

    @Nested
    @DisplayName("Three members scenario from PRD")
    class ThreeMembersScenario {

        /**
         * Alice paid $90 dinner (equal 3-way split → each owes $30) Bob paid $60 taxi (equal 3-way
         * split → each owes $20)
         *
         * <p>Net balances: Alice: +90 - 30 - 20 = +40 (creditor) Bob: -30 + 60 - 20 = +10
         * (creditor) Carol: -30 - 20 = -50 (debtor)
         *
         * <p>Minimized settlements: Carol → Alice: $40 Carol → Bob: $10
         */
        @Test
        @DisplayName("should minimize transactions for three members")
        void shouldMinimizeTransactionsForThreeMembers() {
            // Arrange: Create expenses
            Money amount90 = Money.usd(new BigDecimal("90.00"));
            Money amount60 = Money.usd(new BigDecimal("60.00"));

            // Alice paid $90 dinner, split 3 ways
            Expense dinner =
                    createExpenseWithEqualSplit(
                            alice, amount90, "Dinner", List.of(alice, bob, carol));

            // Bob paid $60 taxi, split 3 ways
            Expense taxi =
                    createExpenseWithEqualSplit(bob, amount60, "Taxi", List.of(alice, bob, carol));

            List<Expense> expenses = List.of(dinner, taxi);

            // Act
            List<Transfer> transfers = service.simplify(expenses);

            // Assert
            assertThat(transfers).hasSize(2);

            // Carol owes money to both Alice and Bob
            assertThat(transfers)
                    .anySatisfy(
                            t -> {
                                assertThat(t.from()).isEqualTo(carol);
                                assertThat(t.to()).isEqualTo(alice);
                                assertThat(t.amount().amount())
                                        .isEqualByComparingTo(new BigDecimal("40.00"));
                            });

            assertThat(transfers)
                    .anySatisfy(
                            t -> {
                                assertThat(t.from()).isEqualTo(carol);
                                assertThat(t.to()).isEqualTo(bob);
                                assertThat(t.amount().amount())
                                        .isEqualByComparingTo(new BigDecimal("10.00"));
                            });
        }

        @Test
        @DisplayName("should return empty list when all balances are zero")
        void shouldReturnEmptyListWhenAllBalancesAreZero() {
            // Alice paid $30, Bob paid $30, Carol paid $30 - each owes their share
            // Everyone is square

            Money amount30 = Money.usd(new BigDecimal("30.00"));

            Expense e1 =
                    createExpenseWithEqualSplit(
                            alice, amount30, "Item1", List.of(alice, bob, carol));
            Expense e2 =
                    createExpenseWithEqualSplit(bob, amount30, "Item2", List.of(alice, bob, carol));
            Expense e3 =
                    createExpenseWithEqualSplit(
                            carol, amount30, "Item3", List.of(alice, bob, carol));

            List<Transfer> transfers = service.simplify(List.of(e1, e2, e3));

            assertThat(transfers).isEmpty();
        }
    }

    @Nested
    @DisplayName("Single debtor multiple creditors")
    class SingleDebtorMultipleCreditors {

        @Test
        @DisplayName("should handle single debtor paying multiple creditors")
        void shouldHandleSingleDebtorMultipleCreditors() {
            // Alice paid $50 for [alice, carol] → each owes $25, Alice is owed $25
            // Bob paid $30 for [bob, carol] → each owes $15, Bob is owed $15
            // Net: Alice +$25, Bob +$15, Carol -$40
            // Carol -> Alice: $25, Carol -> Bob: $15

            Money amount50 = Money.usd(new BigDecimal("50.00"));
            Money amount30 = Money.usd(new BigDecimal("30.00"));

            // Alice paid $50 for something Carol used
            Expense e1 =
                    createExpenseWithEqualSplit(alice, amount50, "Item1", List.of(alice, carol));

            // Bob paid $30 for something Carol used
            Expense e2 = createExpenseWithEqualSplit(bob, amount30, "Item2", List.of(bob, carol));

            List<Transfer> transfers = service.simplify(List.of(e1, e2));

            assertThat(transfers).hasSize(2);

            // Both transfers should be from Carol
            assertThat(transfers).allMatch(t -> t.from().equals(carol));
            assertThat(transfers)
                    .anyMatch(
                            t ->
                                    t.to().equals(alice)
                                            && t.amount()
                                                            .amount()
                                                            .compareTo(new BigDecimal("25.00"))
                                                    == 0);
            assertThat(transfers)
                    .anyMatch(
                            t ->
                                    t.to().equals(bob)
                                            && t.amount()
                                                            .amount()
                                                            .compareTo(new BigDecimal("15.00"))
                                                    == 0);
        }
    }

    @Nested
    @DisplayName("Single debtor single creditor")
    class SingleDebtorSingleCreditor {

        @Test
        @DisplayName("should return single transfer for simple debt")
        void shouldReturnSingleTransferForSimpleDebt() {
            // Alice paid $100 for [alice, carol] → each owes $50
            // Alice is owed $50 from Carol
            Money amount100 = Money.usd(new BigDecimal("100.00"));

            Expense e1 =
                    createExpenseWithEqualSplit(alice, amount100, "Item1", List.of(alice, carol));

            List<Transfer> transfers = service.simplify(List.of(e1));

            assertThat(transfers).hasSize(1);
            assertThat(transfers.get(0).from()).isEqualTo(carol);
            assertThat(transfers.get(0).to()).isEqualTo(alice);
            assertThat(transfers.get(0).amount().amount())
                    .isEqualByComparingTo(new BigDecimal("50.00"));
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should return empty list for empty expenses")
        void shouldReturnEmptyListForEmptyExpenses() {
            List<Transfer> transfers = service.simplify(Collections.emptyList());
            assertThat(transfers).isEmpty();
        }

        @Test
        @DisplayName("should return empty list for null expenses")
        void shouldReturnEmptyListForNullExpenses() {
            List<Transfer> transfers = service.simplify(null);
            assertThat(transfers).isEmpty();
        }

        @Test
        @DisplayName("should handle multiple independent debt cycles")
        void shouldHandleMultipleIndependentDebtCycles() {
            // Create a scenario with 4 people and complex debts
            UserId dave = UserId.of(UUID.randomUUID());

            // Alice $100 for everyone
            Expense e1 =
                    createExpenseWithEqualSplit(
                            alice,
                            Money.usd(new BigDecimal("100.00")),
                            "Party",
                            List.of(alice, bob, carol, dave));

            // Bob $60 for everyone
            Expense e2 =
                    createExpenseWithEqualSplit(
                            bob,
                            Money.usd(new BigDecimal("60.00")),
                            "Dinner",
                            List.of(alice, bob, carol, dave));

            List<Transfer> transfers = service.simplify(List.of(e1, e2));

            // Should have minimized transfers
            assertThat(transfers).isNotEmpty();
            assertThat(transfers).allMatch(t -> !t.amount().isZero());
        }
    }

    // Helper method to create an expense with equal split
    private Expense createExpenseWithEqualSplit(
            UserId payer, Money amount, String desc, List<UserId> participants) {
        BigDecimal splitAmount =
                amount.amount()
                        .divide(BigDecimal.valueOf(participants.size()), 2, RoundingMode.HALF_UP);

        List<ExpenseSplit> splits =
                participants.stream()
                        .map(
                                userId ->
                                        new ExpenseSplit(
                                                userId,
                                                Money.usd(splitAmount),
                                                BigDecimal.valueOf(100.0 / participants.size())
                                                        .setScale(2, RoundingMode.HALF_UP)))
                        .toList();

        return Expense.create(
                GroupId.random(),
                payer,
                amount,
                desc,
                new SplitStrategy.EqualSplit(participants.size()),
                splits);
    }
}
