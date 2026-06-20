package com.mt.expense.app.infrastructure.persistence.mapper;

import com.mt.expense.app.domain.model.Group;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.persistence.entity.GroupJpaEntity;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/** Mapper for converting between Group domain entity and GroupJpaEntity. */
@Component
public class GroupMapper {

    private final ModelMapper modelMapper;

    public GroupMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Group toDomainEntity(GroupJpaEntity entity) {
        if (entity == null) return null;

        Set<UserId> memberIds =
                entity.getMemberIds().stream().map(UserId::of).collect(Collectors.toSet());

        return Group.reconstitute(
                GroupId.of(entity.getId()),
                entity.getName(),
                memberIds,
                UserId.of(entity.getCreatedBy()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public GroupJpaEntity toJpaEntity(Group group) {
        if (group == null) return null;
        GroupJpaEntity entity = new GroupJpaEntity();
        applyDomainToEntity(group, entity);
        return entity;
    }

    /** Copies all scalar domain fields onto an existing (possibly managed) JPA entity. */
    public void applyDomainToEntity(Group group, GroupJpaEntity entity) {
        entity.setId(group.id().value());
        entity.setName(group.name());
        entity.setCreatedBy(group.createdBy().value());
        entity.setCreatedAt(group.createdAt());
        entity.setUpdatedAt(group.updatedAt());
        entity.setMemberIds(
                group.memberIds().stream()
                        .map(UserId::value)
                        .collect(java.util.stream.Collectors.toSet()));
    }
}
