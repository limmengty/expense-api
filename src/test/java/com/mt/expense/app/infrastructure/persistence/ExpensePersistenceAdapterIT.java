package com.mt.expense.app.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.ExpenseSplit;
import com.mt.expense.app.domain.vo.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for ExpensePersistenceAdapter using Testcontainers. Requires Docker to be
 * running.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class ExpensePersistenceAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("expense_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @Autowired private ExpenseRepositoryPort expenseRepository;

    private Expense sampleExpense;

    @BeforeEach
    void setUp() {
        UserId alice = UserId.of(UUID.randomUUID());
        UserId bob = UserId.of(UUID.randomUUID());
        GroupId groupId = GroupId.random();

        List<ExpenseSplit> splits =
                List.of(
                        new ExpenseSplit(
                                alice, Money.usd(new BigDecimal("45.00")), new BigDecimal("50.00")),
                        new ExpenseSplit(
                                bob, Money.usd(new BigDecimal("45.00")), new BigDecimal("50.00")));

        sampleExpense =
                Expense.create(
                        groupId,
                        alice,
                        Money.usd(new BigDecimal("90.00")),
                        "Test Dinner",
                        new SplitStrategy.EqualSplit(2),
                        splits);
    }

    @Test
    @DisplayName("should persist and retrieve expense with splits")
    void shouldPersistAndRetrieveExpenseWithSplits() {
        // Save
        Expense saved = expenseRepository.save(sampleExpense);
        assertThat(saved.id()).isNotNull();

        // Retrieve
        Optional<Expense> retrieved = expenseRepository.findById(saved.id());
        assertThat(retrieved).isPresent();

        Expense expense = retrieved.get();
        assertThat(expense.description()).isEqualTo("Test Dinner");
        assertThat(expense.amount().amount()).isEqualByComparingTo(new BigDecimal("90.00"));
        assertThat(expense.splits()).hasSize(2);
        assertThat(expense.isSettled()).isFalse();
    }

    @Test
    @DisplayName("should find expense by ID")
    void shouldFindExpenseById() {
        Expense saved = expenseRepository.save(sampleExpense);

        Optional<Expense> found = expenseRepository.findById(saved.id());

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(saved.id());
    }

    @Test
    @DisplayName("should return empty for non-existent expense")
    void shouldReturnEmptyForNonExistentExpense() {
        Optional<Expense> found = expenseRepository.findById(ExpenseId.random());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should find all expenses by group ID")
    void shouldFindAllExpensesByGroupId() {
        expenseRepository.save(sampleExpense);

        List<Expense> expenses = expenseRepository.findAllByGroupId(sampleExpense.groupId());

        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).groupId()).isEqualTo(sampleExpense.groupId());
    }

    @Test
    @DisplayName("should delete expense by ID")
    void shouldDeleteExpenseById() {
        Expense saved = expenseRepository.save(sampleExpense);

        expenseRepository.deleteById(saved.id());

        Optional<Expense> found = expenseRepository.findById(saved.id());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should handle expense settlement")
    void shouldHandleExpenseSettlement() {
        Expense saved = expenseRepository.save(sampleExpense);

        Expense settled = saved.settle();
        Expense updated = expenseRepository.save(settled);

        assertThat(updated.isSettled()).isTrue();
    }
}
