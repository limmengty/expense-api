package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.model.ExpenseSplit;
import com.mt.expense.app.domain.model.User;
import java.math.BigDecimal;
import java.util.UUID;

/** Response DTO for a single split entry. */
public record SplitEntryResponse(
        UUID userId, String userName, MoneyDto amount, BigDecimal percentage) {
    public static SplitEntryResponse from(ExpenseSplit split, User payer) {
        return new SplitEntryResponse(
                split.userId().value(),
                payer != null ? payer.name() : "Unknown User",
                new MoneyDto(
                        split.shareAmount().amount(),
                        split.shareAmount().currency().getCurrencyCode()),
                split.percentage());
    }
}
