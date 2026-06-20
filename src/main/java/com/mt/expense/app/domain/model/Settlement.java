package com.mt.expense.app.domain.model;

import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.SettlementId;
import com.mt.expense.app.domain.vo.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain Entity representing a cash settlement payment between two users within a group. Records
 * that {@code from} paid {@code to} the given {@code amount}, reducing their debt.
 */
public final class Settlement {

    private final SettlementId id;
    private final GroupId groupId;
    private final UserId from;
    private final UserId to;
    private final Money amount;
    private final Instant createdAt;

    private Settlement(
            SettlementId id,
            GroupId groupId,
            UserId from,
            UserId to,
            Money amount,
            Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.groupId = Objects.requireNonNull(groupId, "groupId must not be null");
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.to = Objects.requireNonNull(to, "to must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        if (from.equals(to)) {
            throw new IllegalArgumentException("Settlement from and to must be different users");
        }
    }

    public static Settlement create(GroupId groupId, UserId from, UserId to, Money amount) {
        return new Settlement(SettlementId.random(), groupId, from, to, amount, Instant.now());
    }

    public static Settlement reconstitute(
            SettlementId id,
            GroupId groupId,
            UserId from,
            UserId to,
            Money amount,
            Instant createdAt) {
        return new Settlement(id, groupId, from, to, amount, createdAt);
    }

    public SettlementId id() {
        return id;
    }

    public GroupId groupId() {
        return groupId;
    }

    public UserId from() {
        return from;
    }

    public UserId to() {
        return to;
    }

    public Money amount() {
        return amount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settlement s = (Settlement) o;
        return id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
