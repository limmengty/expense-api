package com.mt.expense.app.infrastructure.persistence.repository;

import com.mt.expense.app.infrastructure.persistence.entity.GroupJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository for Group entities. */
@Repository
public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, UUID> {

    @Query("SELECT g FROM GroupJpaEntity g JOIN g.memberIds m WHERE m = :userId")
    java.util.List<GroupJpaEntity> findAllByMemberId(@Param("userId") UUID userId);

    @Query(
            "SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GroupJpaEntity g JOIN g.memberIds m WHERE g.id = :groupId AND m = :userId")
    boolean isMemberOf(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
}
