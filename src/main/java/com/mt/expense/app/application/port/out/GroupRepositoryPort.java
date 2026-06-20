package com.mt.expense.app.application.port.out;

import com.mt.expense.app.domain.model.Group;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import java.util.List;
import java.util.Optional;

/** Outbound Port (Secondary) — Repository interface for group persistence. */
public interface GroupRepositoryPort {

    /**
     * Saves a group to the repository.
     *
     * @param group the group to save
     * @return the saved group
     */
    Group save(Group group);

    /**
     * Finds a group by its ID.
     *
     * @param id the group ID
     * @return an Optional containing the group if found
     */
    Optional<Group> findById(GroupId id);

    /**
     * Checks if a user is a member of the given group.
     *
     * @param groupId the group ID
     * @param userId the user ID
     * @return true if the user is a member of the group
     */
    boolean isMemberOf(GroupId groupId, UserId userId);

    /** Returns all groups the given user is a member of. */
    List<Group> findAllByMemberId(UserId userId);

    /** Adds a member to a group. */
    Group addMember(GroupId groupId, UserId userId);

    /** Removes a member from a group. */
    Group removeMember(GroupId groupId, UserId userId);

    /** Renames a group. */
    Group renameGroup(GroupId groupId, String newName);
}
