package com.mt.expense.app.infrastructure.persistence.mapper;

import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.persistence.entity.UserJpaEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/** Mapper for converting between User domain entity and UserJpaEntity. */
@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public User toDomainEntity(UserJpaEntity entity) {
        if (entity == null) return null;

        return User.reconstitute(
                UserId.of(entity.getId()),
                entity.getKeycloakId(),
                entity.getEmail(),
                entity.getName(),
                entity.getRoles(),
                entity.isEnabled(),
                entity.isVerified(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public UserJpaEntity toJpaEntity(User user) {
        if (user == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.userId().value());
        entity.setKeycloakId(user.keycloakId());
        entity.setEmail(user.email());
        entity.setName(user.name());
        entity.setRoles(user.roles());
        entity.setEnabled(user.enabled());
        entity.setVerified(user.verified());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());

        return entity;
    }
}
