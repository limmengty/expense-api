package com.mt.expense.app.infrastructure.persistence.adapter;

import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.ExpenseSplit;
import com.mt.expense.app.domain.vo.ExpenseId;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.persistence.entity.ExpenseJpaEntity;
import com.mt.expense.app.infrastructure.persistence.entity.ExpenseSplitJpaEntity;
import com.mt.expense.app.infrastructure.persistence.mapper.ExpenseMapper;
import com.mt.expense.app.infrastructure.persistence.repository.ExpenseJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ExpensePersistenceAdapter implements ExpenseRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(ExpensePersistenceAdapter.class);

    private final ExpenseJpaRepository repository;
    private final ExpenseMapper mapper;

    public ExpensePersistenceAdapter(ExpenseJpaRepository repository, ExpenseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Expense save(Expense expense) {
        log.debug("Saving expense: {}", expense.id());

        // Load the existing managed entity so Hibernate dirty-checking handles the update.
        // Using repository.save(entity) on a detached object with a pre-set id triggers
        // em.merge(), which can silently skip cascading changes to OneToMany collections
        // in Hibernate 6.x, causing the UPDATE to never flush.
        ExpenseJpaEntity existing = repository.findById(expense.id().value()).orElse(null);
        if (existing != null) {
            mapper.applyDomainToEntity(expense, existing);
            // Only touch splits if the split data actually changed (avoid unnecessary writes)
            existing.getSplits().clear();
            for (ExpenseSplit split : expense.splits()) {
                ExpenseSplitJpaEntity splitEntity = mapper.toSplitJpaEntity(split);
                splitEntity.setExpense(existing);
                existing.getSplits().add(splitEntity);
            }
            return mapper.toDomainEntity(existing);
        }

        // New expense — create fresh entity
        ExpenseJpaEntity entity = mapper.toJpaEntity(expense);
        for (ExpenseSplit split : expense.splits()) {
            ExpenseSplitJpaEntity splitEntity = mapper.toSplitJpaEntity(split);
            splitEntity.setExpense(entity);
            entity.getSplits().add(splitEntity);
        }
        ExpenseJpaEntity saved = repository.save(entity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Expense> findById(ExpenseId id) {
        Optional<ExpenseJpaEntity> entity = repository.findById(id.value());
        return entity.map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> findAllByGroupId(GroupId groupId) {
        return repository.findByGroupId(groupId.value()).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Expense> findAll(Specification<Expense> spec, Pageable pageable) {
        if (spec == null) {
            return repository.findAll(pageable).map(mapper::toDomainEntity);
        }
        return repository.findAll(toJpaSpec(spec), pageable).map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> findAllBySpec(Specification<Expense> spec) {
        return repository.findAll(toJpaSpec(spec)).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Expense> findUnsettledByGroupAndPayer(GroupId groupId, UserId payerId) {
        return repository
                .findByGroupIdAndPayerIdAndSettledFalse(groupId.value(), payerId.value())
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * Casts Specification&lt;Expense&gt; (domain type) to Specification&lt;ExpenseJpaEntity&gt;.
     * Safe because specs use string-based field access which is type-erased at runtime.
     */
    @SuppressWarnings("unchecked")
    private Specification<ExpenseJpaEntity> toJpaSpec(Specification<Expense> spec) {
        return (Specification<ExpenseJpaEntity>) (Object) spec;
    }

    @Override
    public void deleteById(ExpenseId id) {
        log.debug("Deleting expense: {}", id);
        repository.deleteById(id.value());
    }
}
