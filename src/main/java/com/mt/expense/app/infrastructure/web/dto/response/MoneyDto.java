package com.mt.expense.app.infrastructure.web.dto.response;

import java.math.BigDecimal;

/** DTO for monetary amounts in responses. */
public record MoneyDto(BigDecimal amount, String currency) {}
