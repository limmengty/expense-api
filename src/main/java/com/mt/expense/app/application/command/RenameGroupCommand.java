package com.mt.expense.app.application.command;

import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.infrastructure.security.UserPrincipal;

/** Command object for renaming a group. */
public record RenameGroupCommand(GroupId groupId, String newName, UserPrincipal requestedBy) {
    public RenameGroupCommand {
        if (groupId == null) throw new IllegalArgumentException("groupId must not be null");
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("newName must not be blank");
        if (requestedBy == null) throw new IllegalArgumentException("requestedBy must not be null");
    }
}
