package com.mt.expense.app.infrastructure.persistence.adapter;

import com.mt.expense.app.application.port.out.SettlementRepositoryPort;
import com.mt.expense.app.domain.model.Settlement;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.infrastructure.persistence.mapper.SettlementMapper;
import com.mt.expense.app.infrastructure.persistence.repository.SettlementJpaRepository;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class SettlementPersistenceAdapter implements SettlementRepositoryPort {

    private final SettlementJpaRepository repository;
    private final SettlementMapper mapper;

    public SettlementPersistenceAdapter(
            SettlementJpaRepository repository, SettlementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Settlement save(Settlement settlement) {
        return mapper.toDomainEntity(repository.save(mapper.toJpaEntity(settlement)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Settlement> findAllByGroupId(GroupId groupId) {
        return repository.findAllByGroupId(groupId.value()).stream()
                .map(mapper::toDomainEntity)
                .toList();
    }
}
