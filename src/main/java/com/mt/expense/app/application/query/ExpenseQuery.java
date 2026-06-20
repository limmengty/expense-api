package com.mt.expense.app.application.query;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Query object for filtering expenses. A Java 21 record representing the filter criteria for
 * expense queries.
 */
public record ExpenseQuery(
        UUID groupId, UUID payerId, LocalDate startDate, LocalDate endDate, Boolean settled) {
    public ExpenseQuery {
        // All fields are optional; use null to indicate "not filtered"
    }

    public static ExpenseQuery of(
            UUID groupId, UUID payerId, LocalDate startDate, LocalDate endDate, Boolean settled) {
        return new ExpenseQuery(groupId, payerId, startDate, endDate, settled);
    }

    public static ExpenseQuery empty() {
        return new ExpenseQuery(null, null, null, null, null);
    }
}
