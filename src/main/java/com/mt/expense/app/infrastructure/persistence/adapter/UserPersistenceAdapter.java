package com.mt.expense.app.infrastructure.persistence.adapter;

import com.mt.expense.app.application.port.out.UserRepositoryPort;
import com.mt.expense.app.domain.model.User;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.persistence.entity.UserJpaEntity;
import com.mt.expense.app.infrastructure.persistence.mapper.UserMapper;
import com.mt.expense.app.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** JPA-based implementation of UserRepositoryPort. */
@Component
@Transactional
public class UserPersistenceAdapter implements UserRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(UserPersistenceAdapter.class);

    private final UserJpaRepository repository;
    private final UserMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.userId());
        UserJpaEntity entity = mapper.toJpaEntity(user);
        UserJpaEntity saved = repository.save(entity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        return repository.findById(id.value()).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByKeycloakId(UUID keycloakId) {
        return repository.findByKeycloakId(keycloakId).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByKeycloakId(UUID keycloakId) {
        return repository.existsByKeycloakId(keycloakId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchByEmailOrName(String query, int limit) {
        return repository.searchByEmailOrName(query, PageRequest.of(0, limit)).stream()
                .map(mapper::toDomainEntity)
                .toList();
    }
}
