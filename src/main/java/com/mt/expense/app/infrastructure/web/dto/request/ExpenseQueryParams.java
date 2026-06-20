package com.mt.expense.app.infrastructure.web.dto.request;

import java.time.LocalDate;
import java.util.UUID;

/** Query parameters for expense search. */
public record ExpenseQueryParams(
        UUID groupId,
        UUID payerId,
        LocalDate startDate,
        LocalDate endDate,
        Boolean settled,
        Integer page,
        Integer size,
        String sort) {
    public ExpenseQueryParams {
        if (page == null || page < 0) page = 0;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100; // Max 100 items per page
    }

    public static ExpenseQueryParams of(
            UUID groupId,
            UUID payerId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean settled,
            Integer page,
            Integer size,
            String sort) {
        return new ExpenseQueryParams(
                groupId, payerId, startDate, endDate, settled, page, size, sort);
    }
}
