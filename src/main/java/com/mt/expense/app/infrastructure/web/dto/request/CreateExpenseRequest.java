package com.mt.expense.app.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Request DTO for creating a new expense. */
public record CreateExpenseRequest(
        @NotNull(message = "groupId is required") UUID groupId,
        @NotNull(message = "description is required")
                @Size(max = 500, message = "description must not exceed 500 characters")
                String description,
        @NotNull(message = "totalAmount is required") @Valid MoneyDto totalAmount,
        @NotNull(message = "splitStrategy is required") String splitStrategy,
        @NotNull(message = "splitEntries is required")
                @Size(min = 1, message = "at least one split entry is required")
                @Valid
                List<SplitEntryDto> splitEntries,
        @NotNull(message = "payerId is required") UUID payerId) {
    /** Enum for supported split strategies. */
    public enum SplitStrategyType {
        EQUAL,
        PERCENTAGE,
        EXACT
    }
}
