package com.mt.expense.app.application.command;

import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.security.UserPrincipal;

/** Command object for removing a member from a group. */
public record RemoveGroupMemberCommand(
        GroupId groupId, UserId memberToRemove, UserPrincipal requestedBy) {
    public RemoveGroupMemberCommand {
        if (groupId == null) throw new IllegalArgumentException("groupId must not be null");
        if (memberToRemove == null)
            throw new IllegalArgumentException("memberToRemove must not be null");
        if (requestedBy == null) throw new IllegalArgumentException("requestedBy must not be null");
    }
}
