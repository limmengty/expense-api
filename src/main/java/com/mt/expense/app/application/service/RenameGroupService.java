package com.mt.expense.app.application.service;

import com.mt.expense.app.application.command.RenameGroupCommand;
import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.port.in.RenameGroupUseCase;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of RenameGroupUseCase. */
@org.springframework.stereotype.Service
public final class RenameGroupService implements RenameGroupUseCase {

    private static final Logger log = LoggerFactory.getLogger(RenameGroupService.class);

    private final GroupRepositoryPort groupRepository;

    public RenameGroupService(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public void renameGroup(RenameGroupCommand command) {
        log.info(
                "Renaming group {} to '{}' by {}",
                command.groupId(),
                command.newName(),
                command.requestedBy().userId());

        var group =
                groupRepository
                        .findById(command.groupId())
                        .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        var renamed = group.rename(command.newName());
        groupRepository.save(renamed);

        log.info("Group {} renamed to '{}' successfully", command.groupId(), command.newName());
    }
}
