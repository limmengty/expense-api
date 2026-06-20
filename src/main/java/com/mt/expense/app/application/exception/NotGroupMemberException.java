package com.mt.expense.app.application.exception;

import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;

/** Thrown when a user attempts to perform an action in a group they are not a member of. */
public final class NotGroupMemberException extends BusinessException {

    private final GroupId groupId;
    private final UserId userId;

    public NotGroupMemberException(GroupId groupId, UserId userId) {
        super(
                String.format("User %s is not a member of group %s", userId, groupId),
                "NOT_GROUP_MEMBER");
        this.groupId = groupId;
        this.userId = userId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public UserId userId() {
        return userId;
    }
}
