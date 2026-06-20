package com.mt.expense.app.domain.model;

import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/** Domain Entity representing a group of users who share expenses. */
public final class Group {

    private final GroupId id;
    private final String name;
    private final Set<UserId> memberIds;
    private final UserId createdBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Group(
            GroupId id,
            String name,
            Set<UserId> memberIds,
            UserId createdBy,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.memberIds =
                Set.copyOf(Objects.requireNonNull(memberIds, "memberIds must not be null"));
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (memberIds.isEmpty()) {
            throw new IllegalArgumentException("A group must have at least one member");
        }
    }

    public static Group create(String name, UserId creator, Set<UserId> initialMembers) {
        Instant now = Instant.now();
        Set<UserId> allMembers =
                new java.util.HashSet<>(initialMembers != null ? initialMembers : Set.of());
        allMembers.add(creator);
        return new Group(GroupId.random(), name, allMembers, creator, now, now);
    }

    public static Group reconstitute(
            GroupId id,
            String name,
            Set<UserId> memberIds,
            UserId createdBy,
            Instant createdAt,
            Instant updatedAt) {
        return new Group(id, name, memberIds, createdBy, createdAt, updatedAt);
    }

    public Group addMember(UserId userId) {
        Set<UserId> newMembers = new java.util.HashSet<>(memberIds);
        newMembers.add(userId);
        return new Group(id, name, newMembers, createdBy, createdAt, Instant.now());
    }

    public Group removeMember(UserId userId) {
        if (memberIds.size() <= 1) {
            throw new IllegalStateException("Cannot remove the last member from a group");
        }
        Set<UserId> newMembers = new java.util.HashSet<>(memberIds);
        newMembers.remove(userId);
        return new Group(id, name, newMembers, createdBy, createdAt, Instant.now());
    }

    public Group rename(String newName) {
        return new Group(id, newName, memberIds, createdBy, createdAt, Instant.now());
    }

    public boolean isMember(UserId userId) {
        return memberIds.contains(userId);
    }

    // Getters
    public GroupId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Set<UserId> memberIds() {
        return memberIds;
    }

    public UserId createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
