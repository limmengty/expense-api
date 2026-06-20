package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** DTO for monetary amounts. */
public record MoneyDto(
        @NotNull(message = "amount is required") @Positive(message = "amount must be positive")
                BigDecimal amount,
        @NotNull(message = "currency is required")
                @Size(min = 3, max = 3, message = "currency must be a 3-letter code")
                String currency) {
    public MoneyDto {
        if (currency != null) {
            currency = currency.toUpperCase();
        }
    }
}
