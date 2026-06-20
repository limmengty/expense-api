package com.mt.expense.app.application.exception;

import com.mt.expense.app.domain.vo.GroupId;

/** Thrown when a group cannot be found. */
public final class GroupNotFoundException extends BusinessException {

    private final GroupId groupId;

    public GroupNotFoundException(GroupId groupId) {
        super("Group not found: " + groupId, "GROUP_NOT_FOUND");
        this.groupId = groupId;
    }

    public GroupId groupId() {
        return groupId;
    }
}
