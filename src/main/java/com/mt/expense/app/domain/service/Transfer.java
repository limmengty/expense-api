package com.mt.expense.app.domain.service;

import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.UserId;

/** Represents a single money transfer between two users to settle a debt. */
public record Transfer(UserId from, UserId to, Money amount) {
    public Transfer {
        if (amount.isZero()) {
            throw new IllegalArgumentException("Transfer amount must be non-zero");
        }
    }
}
