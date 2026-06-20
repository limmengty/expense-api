package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.command.RenameGroupCommand;

/** Inbound Port — Use case for renaming a group. */
public interface RenameGroupUseCase {
    void renameGroup(RenameGroupCommand command);
}
