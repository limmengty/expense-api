package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.model.Settlement;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID groupId,
        UUID fromUserId,
        UUID toUserId,
        BigDecimal amount,
        String currency,
        Instant createdAt) {
    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.id().value(),
                settlement.groupId().value(),
                settlement.from().value(),
                settlement.to().value(),
                settlement.amount().amount(),
                settlement.amount().currency().getCurrencyCode(),
                settlement.createdAt());
    }
}
