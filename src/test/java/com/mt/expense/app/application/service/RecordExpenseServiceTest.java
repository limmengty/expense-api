package com.mt.expense.app.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mt.expense.app.application.command.RecordExpenseCommand;
import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.exception.NotGroupMemberException;
import com.mt.expense.app.application.exception.SplitMismatchException;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.application.query.SplitEntry;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.Group;
import com.mt.expense.app.domain.vo.*;
import com.mt.expense.app.infrastructure.security.UserPrincipal;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for RecordExpenseService with mocked ports. */
@ExtendWith(MockitoExtension.class)
class RecordExpenseServiceTest {

    @Mock private ExpenseRepositoryPort expenseRepository;

    @Mock private GroupRepositoryPort groupRepository;

    private RecordExpenseService service;

    private UserId payerId;
    private UserId memberId;
    private GroupId groupId;
    private Group group;
    private UserPrincipal principal;
    private Money totalAmount;

    @BeforeEach
    void setUp() {
        service = new RecordExpenseService(expenseRepository, groupRepository);

        payerId = UserId.of(UUID.randomUUID());
        memberId = UserId.of(UUID.randomUUID());
        groupId = GroupId.random();
        totalAmount = Money.usd(new BigDecimal("90.00"));

        principal = new UserPrincipal(payerId, "payer@test.com", List.of("ROLE_USER"));

        Set<UserId> members = new HashSet<>(Set.of(payerId, memberId));
        group =
                Group.reconstitute(
                        groupId,
                        "Test Group",
                        members,
                        payerId,
                        java.time.Instant.now(),
                        java.time.Instant.now());
    }

    @Nested
    @DisplayName("Successful expense recording")
    class SuccessfulRecording {

        @Test
        @DisplayName("should record expense when all validations pass")
        void shouldRecordExpenseWhenAllValidationsPass() {
            // Given
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

            List<SplitEntry> entries =
                    List.of(
                            new SplitEntry(payerId, new BigDecimal("45.00"), null),
                            new SplitEntry(memberId, new BigDecimal("45.00"), null));

            RecordExpenseCommand command =
                    new RecordExpenseCommand(
                            groupId,
                            payerId,
                            totalAmount,
                            "Dinner",
                            new SplitStrategy.ExactAmountSplit(
                                    List.of(
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    payerId, new BigDecimal("45.00")),
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    memberId, new BigDecimal("45.00")))),
                            entries,
                            principal);

            // When
            ExpenseId result = service.recordExpense(command);

            // Then
            assertThat(result).isNotNull();
            verify(expenseRepository).save(any(Expense.class));
        }
    }

    @Nested
    @DisplayName("Validation failures")
    class ValidationFailures {

        @Test
        @DisplayName("should throw GroupNotFoundException when group does not exist")
        void shouldThrowWhenGroupNotFound() {
            // Given
            when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

            List<SplitEntry> entries =
                    List.of(
                            new SplitEntry(payerId, new BigDecimal("45.00"), null),
                            new SplitEntry(memberId, new BigDecimal("45.00"), null));

            RecordExpenseCommand command =
                    new RecordExpenseCommand(
                            groupId,
                            payerId,
                            totalAmount,
                            "Dinner",
                            new SplitStrategy.ExactAmountSplit(
                                    List.of(
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    payerId, new BigDecimal("45.00")),
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    memberId, new BigDecimal("45.00")))),
                            entries,
                            principal);

            // When/Then
            assertThatThrownBy(() -> service.recordExpense(command))
                    .isInstanceOf(GroupNotFoundException.class)
                    .hasMessageContaining(groupId.toString());

            verifyNoInteractions(expenseRepository);
        }

        @Test
        @DisplayName("should throw NotGroupMemberException when payer is not a member")
        void shouldThrowWhenPayerNotGroupMember() {
            // Given
            UserId nonMember = UserId.of(UUID.randomUUID());
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

            List<SplitEntry> entries =
                    List.of(
                            new SplitEntry(nonMember, new BigDecimal("45.00"), null),
                            new SplitEntry(memberId, new BigDecimal("45.00"), null));

            RecordExpenseCommand command =
                    new RecordExpenseCommand(
                            groupId,
                            nonMember,
                            totalAmount,
                            "Dinner",
                            new SplitStrategy.ExactAmountSplit(
                                    List.of(
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    nonMember, new BigDecimal("45.00")),
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    memberId, new BigDecimal("45.00")))),
                            entries,
                            principal);

            // When/Then
            assertThatThrownBy(() -> service.recordExpense(command))
                    .isInstanceOf(NotGroupMemberException.class);

            verifyNoInteractions(expenseRepository);
        }

        @Test
        @DisplayName("should throw SplitMismatchException when splits don't sum to total")
        void shouldThrowWhenSplitAmountsMismatch() {
            // Given
            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            // Note: expenseRepository.save() is NOT stubbed because validation throws before saving

            // Splits sum to $89 but total is $90
            List<SplitEntry> entries =
                    List.of(
                            new SplitEntry(payerId, new BigDecimal("45.00"), null),
                            new SplitEntry(memberId, new BigDecimal("44.00"), null) // Wrong total
                            );

            RecordExpenseCommand command =
                    new RecordExpenseCommand(
                            groupId,
                            payerId,
                            totalAmount,
                            "Dinner",
                            new SplitStrategy.ExactAmountSplit(
                                    List.of(
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    payerId, new BigDecimal("45.00")),
                                            new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                    memberId, new BigDecimal("44.00")))),
                            entries,
                            principal);

            // When/Then
            assertThatThrownBy(() -> service.recordExpense(command))
                    .isInstanceOf(SplitMismatchException.class)
                    .hasMessageContaining("89")
                    .hasMessageContaining("90");

            verifyNoInteractions(expenseRepository);
        }
    }
}
