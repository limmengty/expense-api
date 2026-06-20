package com.mt.expense.app.application.service;

import com.mt.expense.app.application.command.RecordSettlementCommand;
import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.port.in.RecordSettlementUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.application.port.out.SettlementRepositoryPort;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.Settlement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecordSettlementService implements RecordSettlementUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordSettlementService.class);

    private final SettlementRepositoryPort settlementRepository;
    private final GroupRepositoryPort groupRepository;
    private final ExpenseRepositoryPort expenseRepository;

    public RecordSettlementService(
            SettlementRepositoryPort settlementRepository,
            GroupRepositoryPort groupRepository,
            ExpenseRepositoryPort expenseRepository) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Settlement record(RecordSettlementCommand command) {
        log.info(
                "Recording settlement: {} → {} in group {} for {}",
                command.from(),
                command.to(),
                command.groupId(),
                command.amount());

        if (groupRepository.findById(command.groupId()).isEmpty()) {
            throw new GroupNotFoundException(command.groupId());
        }

        Settlement settlement =
                Settlement.create(
                        command.groupId(), command.from(), command.to(), command.amount());

        Settlement saved = settlementRepository.save(settlement);

        // Settle all unsettled expenses in the group where the 'from' user is the payer.
        // A → B settlement means A paid B (as recorded in the expenses), so we settle
        // all expenses A paid on behalf of B that are still outstanding.
        List<Expense> unsettled =
                expenseRepository.findUnsettledByGroupAndPayer(command.groupId(), command.from());
        for (Expense expense : unsettled) {
            Expense settled = expense.settle();
            expenseRepository.save(settled);
        }

        log.info("Settled {} expenses after recording settlement {}", unsettled.size(), saved.id());
        return saved;
    }
}
