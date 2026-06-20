package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.vo.SplitStrategy;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Response DTO for expense details. */
public record ExpenseResponse(
        UUID expenseId,
        UUID groupId,
        UUID payerId,
        String payerName,
        String payerEmail,
        MoneyDto amount,
        String description,
        String splitStrategy,
        List<SplitEntryResponse> splits,
        boolean settled,
        Instant createdAt) {
    public static ExpenseResponse from(Expense expense, User payer) {
        String payerName = payer != null ? payer.name() : "Unknown User";
        String payerEmail = payer != null ? payer.email() : null;
        return new ExpenseResponse(
                expense.id().value(),
                expense.groupId().value(),
                expense.payerId().value(),
                payerName,
                payerEmail,
                new MoneyDto(
                        expense.amount().amount(), expense.amount().currency().getCurrencyCode()),
                expense.description(),
                mapSplitStrategy(expense.splitStrategy()),
                expense.splits().stream().map(s -> SplitEntryResponse.from(s, payer)).toList(),
                expense.isSettled(),
                expense.createdAt());
    }

    private static String mapSplitStrategy(SplitStrategy strategy) {
        return switch (strategy) {
            case SplitStrategy.EqualSplit s -> "EQUAL";
            case SplitStrategy.PercentageSplit s -> "PERCENTAGE";
            case SplitStrategy.ExactAmountSplit s -> "EXACT";
        };
    }
}
