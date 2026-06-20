package com.mt.expense.app.infrastructure.persistence.repository;

import com.mt.expense.app.infrastructure.persistence.entity.UserJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** JPA Repository for User entity. */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByKeycloakId(UUID keycloakId);

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByKeycloakId(UUID keycloakId);

    boolean existsByEmail(String email);

    @Query(
            "SELECT u FROM UserJpaEntity u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<UserJpaEntity> searchByEmailOrName(@Param("q") String q, Pageable pageable);
}
