package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.command.RemoveGroupMemberCommand;

/** Inbound Port — Use case for removing a member from a group. */
public interface RemoveGroupMemberUseCase {
    void removeMember(RemoveGroupMemberCommand command);
}
