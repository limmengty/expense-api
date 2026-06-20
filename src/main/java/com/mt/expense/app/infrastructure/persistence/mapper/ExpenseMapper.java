package com.mt.expense.app.infrastructure.persistence.mapper;

import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.ExpenseSplit;
import com.mt.expense.app.domain.vo.*;
import com.mt.expense.app.infrastructure.persistence.entity.ExpenseJpaEntity;
import com.mt.expense.app.infrastructure.persistence.entity.ExpenseSplitJpaEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/** Mapper for converting between Domain entities and JPA entities. */
@Component
public class ExpenseMapper {

    private final ModelMapper modelMapper;

    public ExpenseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Expense toDomainEntity(ExpenseJpaEntity entity) {
        if (entity == null) return null;

        String currency = entity.getCurrency();
        List<ExpenseSplit> splits =
                entity.getSplits().stream()
                        .map(s -> splitEntityToDomain(s, currency))
                        .collect(Collectors.toList());

        Money amount = Money.of(entity.getAmount(), entity.getCurrency());

        return Expense.reconstitute(
                ExpenseId.of(entity.getId()),
                GroupId.of(entity.getGroupId()),
                UserId.of(entity.getPayerId()),
                amount,
                entity.getDescription(),
                stringToSplitStrategy(entity.getSplitStrategy(), splits),
                splits,
                entity.isSettled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public ExpenseJpaEntity toJpaEntity(Expense expense) {
        if (expense == null) return null;
        ExpenseJpaEntity entity = new ExpenseJpaEntity();
        applyDomainToEntity(expense, entity);
        return entity;
    }

    /** Copies all scalar domain fields onto an existing (possibly managed) JPA entity. */
    public void applyDomainToEntity(Expense expense, ExpenseJpaEntity entity) {
        entity.setId(expense.id().value());
        entity.setGroupId(expense.groupId().value());
        entity.setPayerId(expense.payerId().value());
        entity.setAmount(expense.amount().amount());
        entity.setCurrency(expense.amount().getCurrencyCode());
        entity.setDescription(expense.description());
        entity.setSplitStrategy(splitStrategyToString(expense.splitStrategy()));
        entity.setSettled(expense.isSettled());
        entity.setCreatedAt(expense.createdAt());
        entity.setUpdatedAt(expense.updatedAt());
        if (expense.isSettled() && entity.getSettledAt() == null) {
            entity.setSettledAt(java.time.Instant.now());
        }
    }

    public ExpenseSplitJpaEntity toSplitJpaEntity(ExpenseSplit split) {
        if (split == null) return null;

        ExpenseSplitJpaEntity entity = new ExpenseSplitJpaEntity();
        entity.setUserId(split.userId().value());
        entity.setAmount(split.shareAmount().amount());
        entity.setPercentage(split.percentage());
        return entity;
    }

    private ExpenseSplit splitEntityToDomain(ExpenseSplitJpaEntity entity, String currency) {
        return new ExpenseSplit(
                UserId.of(entity.getUserId()),
                Money.of(entity.getAmount(), currency),
                entity.getPercentage());
    }

    private SplitStrategy stringToSplitStrategy(String strategy, List<ExpenseSplit> splits) {
        if (strategy == null) return null;
        return switch (strategy) {
            case "EQUAL" -> new SplitStrategy.EqualSplit(splits.size());
            case "PERCENTAGE" ->
                    new SplitStrategy.PercentageSplit(
                            splits.stream()
                                    .map(
                                            s ->
                                                    new SplitStrategy.PercentageSplit
                                                            .PercentageEntry(
                                                            s.userId(), s.percentage()))
                                    .toList());
            case "EXACT" ->
                    new SplitStrategy.ExactAmountSplit(
                            splits.stream()
                                    .map(
                                            s ->
                                                    new SplitStrategy.ExactAmountSplit.AmountEntry(
                                                            s.userId(), s.shareAmount().amount()))
                                    .toList());
            default -> throw new IllegalArgumentException("Unknown split strategy: " + strategy);
        };
    }

    private String splitStrategyToString(SplitStrategy strategy) {
        if (strategy == null) return null;
        return switch (strategy) {
            case SplitStrategy.EqualSplit s -> "EQUAL";
            case SplitStrategy.PercentageSplit s -> "PERCENTAGE";
            case SplitStrategy.ExactAmountSplit s -> "EXACT";
        };
    }
}
