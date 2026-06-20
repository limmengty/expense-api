package com.mt.expense.app.domain.vo;

import java.util.UUID;

public final class SettlementId {

    private final UUID value;

    private SettlementId(UUID value) {
        this.value = value;
    }

    public static SettlementId of(UUID value) {
        return new SettlementId(value);
    }

    public static SettlementId random() {
        return new SettlementId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettlementId that = (SettlementId) o;
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
