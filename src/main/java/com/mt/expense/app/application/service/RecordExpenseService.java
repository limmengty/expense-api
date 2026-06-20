package com.mt.expense.app.application.service;

import com.mt.expense.app.application.command.RecordExpenseCommand;
import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.exception.NotGroupMemberException;
import com.mt.expense.app.application.exception.SplitMismatchException;
import com.mt.expense.app.application.port.in.RecordExpenseUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.application.query.SplitEntry;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.ExpenseSplit;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.SplitStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Service
public final class RecordExpenseService implements RecordExpenseUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordExpenseService.class);
    private final ExpenseRepositoryPort expenseRepository;
    private final GroupRepositoryPort groupRepository;

    public RecordExpenseService(
            ExpenseRepositoryPort expenseRepository, GroupRepositoryPort groupRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public com.mt.expense.app.domain.vo.ExpenseId recordExpense(RecordExpenseCommand command) {
        log.info(
                "Recording expense for group {} by payer {}", command.groupId(), command.payerId());

        var group =
                groupRepository
                        .findById(command.groupId())
                        .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        if (!group.isMember(command.payerId())) {
            throw new NotGroupMemberException(command.groupId(), command.payerId());
        }

        List<ExpenseSplit> splits = buildSplits(command);
        Expense expense;
        try {
            expense =
                    Expense.create(
                            command.groupId(),
                            command.payerId(),
                            command.totalAmount(),
                            command.description(),
                            command.splitStrategy(),
                            splits);
        } catch (com.mt.expense.app.domain.exception.SplitMismatchException e) {
            throw new SplitMismatchException(e.sumOfSplits(), e.totalAmount());
        }

        Expense saved = expenseRepository.save(expense);
        log.info("Expense recorded: {}", saved.id());
        return saved.id();
    }

    private List<ExpenseSplit> buildSplits(RecordExpenseCommand command) {
        SplitStrategy strategy = command.splitStrategy();
        Money amount = command.totalAmount();
        List<SplitEntry> entries = command.splitEntries();

        return switch (strategy) {
            case SplitStrategy.EqualSplit s -> buildEqualSplits(amount, entries);
            case SplitStrategy.PercentageSplit s -> buildPercentageSplits(amount, s.entries());
            case SplitStrategy.ExactAmountSplit s -> buildExactSplits(amount, s.entries());
        };
    }

    private List<ExpenseSplit> buildEqualSplits(Money amount, List<SplitEntry> entries) {
        int count = entries.size();
        BigDecimal base = amount.amount().divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        BigDecimal remainder = amount.amount().subtract(base.multiply(BigDecimal.valueOf(count)));
        String currency = amount.getCurrencyCode();
        BigDecimal cent = new BigDecimal("0.01");
        // how many people receive an extra cent (e.g. $0.01 remainder → 1 person)
        int extraCents = remainder.movePointRight(2).setScale(0, RoundingMode.DOWN).intValue();

        return java.util.stream.IntStream.range(0, count)
                .mapToObj(
                        i -> {
                            BigDecimal splitAmt = base.add(i < extraCents ? cent : BigDecimal.ZERO);
                            BigDecimal pct =
                                    BigDecimal.valueOf(100.0 / count)
                                            .setScale(2, RoundingMode.HALF_UP);
                            return new ExpenseSplit(
                                    entries.get(i).userId(), Money.of(splitAmt, currency), pct);
                        })
                .collect(Collectors.toList());
    }

    private List<ExpenseSplit> buildPercentageSplits(
            Money amount, List<SplitStrategy.PercentageSplit.PercentageEntry> entries) {
        String currency = amount.getCurrencyCode();
        return entries.stream()
                .map(
                        e -> {
                            BigDecimal pct = e.percentage();
                            BigDecimal amt =
                                    amount.amount()
                                            .multiply(pct)
                                            .divide(
                                                    BigDecimal.valueOf(100),
                                                    2,
                                                    RoundingMode.HALF_UP);
                            return new ExpenseSplit(e.userId(), Money.of(amt, currency), pct);
                        })
                .collect(Collectors.toList());
    }

    private List<ExpenseSplit> buildExactSplits(
            Money amount, List<SplitStrategy.ExactAmountSplit.AmountEntry> entries) {
        String currency = amount.getCurrencyCode();
        return entries.stream()
                .map(e -> new ExpenseSplit(e.userId(), Money.of(e.amount(), currency), null))
                .collect(Collectors.toList());
    }
}
