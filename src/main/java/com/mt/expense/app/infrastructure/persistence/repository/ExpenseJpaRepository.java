package com.mt.expense.app.infrastructure.persistence.repository;

import com.mt.expense.app.infrastructure.persistence.entity.ExpenseJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for Expense entities. Extends JpaSpecificationExecutor for dynamic
 * query building.
 */
@Repository
public interface ExpenseJpaRepository
        extends JpaRepository<ExpenseJpaEntity, UUID>, JpaSpecificationExecutor<ExpenseJpaEntity> {

    List<ExpenseJpaEntity> findByGroupId(UUID groupId);

    List<ExpenseJpaEntity> findByGroupIdAndPayerIdAndSettledFalse(UUID groupId, UUID payerId);
}
