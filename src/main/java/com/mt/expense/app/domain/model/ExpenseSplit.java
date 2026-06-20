package com.mt.expense.app.domain.model;

import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.UserId;
import java.math.BigDecimal;
import java.util.Objects;

/** Represents a single participant's share of an expense. Immutable value-based entity. */
public final class ExpenseSplit {

    private final UserId userId;
    private final Money shareAmount;
    private final BigDecimal percentage;

    public ExpenseSplit(UserId userId, Money shareAmount, BigDecimal percentage) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.shareAmount = Objects.requireNonNull(shareAmount, "shareAmount must not be null");
        this.percentage = percentage; // nullable for exact split strategy
    }

    public UserId userId() {
        return userId;
    }

    public Money shareAmount() {
        return shareAmount;
    }

    public BigDecimal percentage() {
        return percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseSplit that = (ExpenseSplit) o;
        return userId.equals(that.userId)
                && shareAmount.equals(that.shareAmount)
                && Objects.equals(percentage, that.percentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, shareAmount, percentage);
    }

    @Override
    public String toString() {
        return "ExpenseSplit{userId="
                + userId
                + ", shareAmount="
                + shareAmount
                + ", percentage="
                + percentage
                + "}";
    }
}
