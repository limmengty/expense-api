package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordSettlementRequest(
        @NotNull UUID groupId,
        @NotNull UUID fromUserId,
        @NotNull UUID toUserId,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
                BigDecimal amount,
        String currency) {
    public String resolvedCurrency() {
        return (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";
    }
}
