package com.mt.expense.app.domain.vo;

import java.util.UUID;

public final class ExpenseId {
    private final UUID value;

    private ExpenseId(UUID value) {
        this.value = value;
    }

    public static ExpenseId of(UUID value) {
        return new ExpenseId(value);
    }

    public static ExpenseId of(String value) {
        return new ExpenseId(UUID.fromString(value));
    }

    public static ExpenseId random() {
        return new ExpenseId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseId that = (ExpenseId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
