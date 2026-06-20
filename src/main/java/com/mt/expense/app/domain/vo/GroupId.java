package com.mt.expense.app.domain.vo;

import java.util.UUID;

public final class GroupId {
    private final UUID value;

    private GroupId(UUID value) {
        this.value = value;
    }

    public static GroupId of(UUID value) {
        return new GroupId(value);
    }

    public static GroupId of(String value) {
        return new GroupId(UUID.fromString(value));
    }

    public static GroupId random() {
        return new GroupId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupId that = (GroupId) o;
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
