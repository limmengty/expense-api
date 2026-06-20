package com.mt.expense.app.application.service;

import com.mt.expense.app.application.command.RemoveGroupMemberCommand;
import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.exception.NotGroupMemberException;
import com.mt.expense.app.application.port.in.RemoveGroupMemberUseCase;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of RemoveGroupMemberUseCase. */
@org.springframework.stereotype.Service
public final class RemoveGroupMemberService implements RemoveGroupMemberUseCase {

    private static final Logger log = LoggerFactory.getLogger(RemoveGroupMemberService.class);

    private final GroupRepositoryPort groupRepository;

    public RemoveGroupMemberService(GroupRepositoryPort groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public void removeMember(RemoveGroupMemberCommand command) {
        log.info(
                "Removing member {} from group {} by {}",
                command.memberToRemove(),
                command.groupId(),
                command.requestedBy().userId());

        var group =
                groupRepository
                        .findById(command.groupId())
                        .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        if (!group.isMember(command.memberToRemove())) {
            throw new NotGroupMemberException(command.groupId(), command.memberToRemove());
        }

        var updated = group.removeMember(command.memberToRemove());
        groupRepository.save(updated);

        log.info(
                "Member {} removed from group {} successfully",
                command.memberToRemove(),
                command.groupId());
    }
}
