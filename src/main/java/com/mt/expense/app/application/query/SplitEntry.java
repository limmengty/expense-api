package com.mt.expense.app.application.query;

import com.mt.expense.app.domain.vo.UserId;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single split entry in a expense split configuration. Either amount OR percentage
 * should be provided depending on the split strategy.
 */
public record SplitEntry(UserId userId, BigDecimal amount, BigDecimal percentage) {
    public SplitEntry {
        if (userId == null) throw new IllegalArgumentException("userId must not be null");
    }

    /** Creates a split entry with an exact amount. */
    public static SplitEntry ofAmount(UUID userId, BigDecimal amount) {
        return new SplitEntry(UserId.of(userId), amount, null);
    }

    /** Creates a split entry with a percentage. */
    public static SplitEntry ofPercentage(UUID userId, BigDecimal percentage) {
        return new SplitEntry(UserId.of(userId), null, percentage);
    }
}
