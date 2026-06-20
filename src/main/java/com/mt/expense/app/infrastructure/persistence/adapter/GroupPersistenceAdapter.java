package com.mt.expense.app.infrastructure.persistence.adapter;

import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.domain.model.Group;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.persistence.entity.GroupJpaEntity;
import com.mt.expense.app.infrastructure.persistence.mapper.GroupMapper;
import com.mt.expense.app.infrastructure.persistence.repository.GroupJpaRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-based implementation of GroupRepositoryPort. */
@Component
@Transactional
public class GroupPersistenceAdapter implements GroupRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(GroupPersistenceAdapter.class);

    private final GroupJpaRepository repository;
    private final GroupMapper mapper;

    public GroupPersistenceAdapter(GroupJpaRepository repository, GroupMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Group save(Group group) {
        log.debug("Saving group: {}", group.id());
        GroupJpaEntity existing = repository.findById(group.id().value()).orElse(null);
        if (existing != null) {
            mapper.applyDomainToEntity(group, existing);
            return mapper.toDomainEntity(existing);
        }
        GroupJpaEntity entity = mapper.toJpaEntity(group);
        return mapper.toDomainEntity(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Group> findById(GroupId id) {
        return repository.findById(id.value()).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMemberOf(GroupId groupId, UserId userId) {
        return repository.isMemberOf(groupId.value(), userId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> findAllByMemberId(UserId userId) {
        return repository.findAllByMemberId(userId.value()).stream()
                .map(mapper::toDomainEntity)
                .toList();
    }

    @Override
    public Group addMember(GroupId groupId, UserId userId) {
        log.debug("Adding member {} to group {}", userId, groupId);
        GroupJpaEntity entity =
                repository
                        .findById(groupId.value())
                        .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        entity.addMember(userId.value());
        GroupJpaEntity saved = repository.save(entity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Group removeMember(GroupId groupId, UserId userId) {
        log.debug("Removing member {} from group {}", userId, groupId);
        GroupJpaEntity entity =
                repository
                        .findById(groupId.value())
                        .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        entity.removeMember(userId.value());
        GroupJpaEntity saved = repository.save(entity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    public Group renameGroup(GroupId groupId, String newName) {
        log.debug("Renaming group {} to '{}'", groupId, newName);
        GroupJpaEntity entity =
                repository
                        .findById(groupId.value())
                        .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));
        entity.setName(newName);
        entity.setUpdatedAt(java.time.Instant.now());
        GroupJpaEntity saved = repository.save(entity);
        return mapper.toDomainEntity(saved);
    }
}
