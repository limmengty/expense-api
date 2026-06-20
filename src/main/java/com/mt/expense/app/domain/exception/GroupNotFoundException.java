package com.mt.expense.app.domain.exception;

import com.mt.expense.app.domain.vo.GroupId;

/** Thrown when a group cannot be found. */
public final class GroupNotFoundException extends RuntimeException {

    private final GroupId groupId;

    public GroupNotFoundException(GroupId groupId) {
        super("Group not found: " + groupId);
        this.groupId = groupId;
    }

    public GroupId groupId() {
        return groupId;
    }
}
