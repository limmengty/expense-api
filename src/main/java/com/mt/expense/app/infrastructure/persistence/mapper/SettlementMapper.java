package com.mt.expense.app.infrastructure.persistence.mapper;

import com.mt.expense.app.domain.model.Settlement;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.SettlementId;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.persistence.entity.SettlementJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementJpaEntity toJpaEntity(Settlement domain) {
        SettlementJpaEntity entity = new SettlementJpaEntity();
        entity.setId(domain.id().value());
        entity.setGroupId(domain.groupId().value());
        entity.setFromUserId(domain.from().value());
        entity.setToUserId(domain.to().value());
        entity.setAmount(domain.amount().amount());
        entity.setCurrency(domain.amount().currency().getCurrencyCode());
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }

    public Settlement toDomainEntity(SettlementJpaEntity entity) {
        return Settlement.reconstitute(
                SettlementId.of(entity.getId()),
                GroupId.of(entity.getGroupId()),
                UserId.of(entity.getFromUserId()),
                UserId.of(entity.getToUserId()),
                Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getCreatedAt());
    }
}
