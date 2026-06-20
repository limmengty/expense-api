package com.mt.expense.app.domain.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sealed interface representing a strategy for splitting an expense among participants.
 * Implementations must be exhaustively handled in switch expressions at compile time.
 */
public sealed interface SplitStrategy
        permits SplitStrategy.EqualSplit,
                SplitStrategy.PercentageSplit,
                SplitStrategy.ExactAmountSplit {

    /**
     * Equal split — total amount divided equally among all participants. Any remainder is
     * distributed to the first participants in order.
     */
    record EqualSplit(int participantCount) implements SplitStrategy {
        public EqualSplit {
            if (participantCount < 1) {
                throw new IllegalArgumentException("Participant count must be at least 1");
            }
        }
    }

    /**
     * Percentage-based split — each participant assigned a percentage of the total. Percentages
     * must sum to exactly 100.
     */
    record PercentageSplit(List<PercentageEntry> entries) implements SplitStrategy {
        public PercentageSplit {
            if (entries == null || entries.isEmpty()) {
                throw new IllegalArgumentException("Entries must not be empty");
            }
            BigDecimal sum =
                    entries.stream()
                            .map(e -> e.percentage())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw new IllegalArgumentException("Percentages must sum to 100, got: " + sum);
            }
        }

        public record PercentageEntry(UserId userId, BigDecimal percentage) {
            public PercentageEntry {
                if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Percentage must be non-negative");
                }
            }
        }
    }

    /**
     * Exact amount split — each participant assigned a specific amount. Amounts must sum to exactly
     * the expense total.
     */
    record ExactAmountSplit(List<AmountEntry> entries) implements SplitStrategy {
        public ExactAmountSplit {
            if (entries == null || entries.isEmpty()) {
                throw new IllegalArgumentException("Entries must not be empty");
            }
        }

        public record AmountEntry(UserId userId, BigDecimal amount) {
            public AmountEntry {
                if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Amount must be non-negative");
                }
            }
        }
    }
}
