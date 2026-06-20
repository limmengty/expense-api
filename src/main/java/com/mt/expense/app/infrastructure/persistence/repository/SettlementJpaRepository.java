package com.mt.expense.app.infrastructure.persistence.repository;

import com.mt.expense.app.infrastructure.persistence.entity.SettlementJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, UUID> {

    List<SettlementJpaEntity> findAllByGroupId(UUID groupId);
}
