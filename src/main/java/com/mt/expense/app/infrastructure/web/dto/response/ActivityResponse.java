package com.mt.expense.app.infrastructure.web.dto.response;

import java.math.BigDecimal;

public record ActivityResponse(
        String id,
        String type,
        String description,
        BigDecimal amount,
        String date,
        String groupName,
        String userName,
        Boolean isPositive) {}
