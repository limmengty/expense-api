package com.mt.expense.app.application.service;

import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.application.port.in.CalculateGroupBalancesUseCase;
import com.mt.expense.app.application.port.out.ExpenseRepositoryPort;
import com.mt.expense.app.application.port.out.GroupRepositoryPort;
import com.mt.expense.app.application.port.out.SettlementRepositoryPort;
import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.Settlement;
import com.mt.expense.app.domain.service.DebtSimplificationService;
import com.mt.expense.app.domain.service.Transfer;
import com.mt.expense.app.domain.vo.GroupId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of CalculateGroupBalancesUseCase. Uses DebtSimplificationService to minimize
 * settlement transactions.
 */
@org.springframework.stereotype.Service
public final class CalculateGroupBalancesService implements CalculateGroupBalancesUseCase {

    private static final Logger log = LoggerFactory.getLogger(CalculateGroupBalancesService.class);

    private final ExpenseRepositoryPort expenseRepository;
    private final GroupRepositoryPort groupRepository;
    private final SettlementRepositoryPort settlementRepository;
    private final DebtSimplificationService debtSimplificationService;

    public CalculateGroupBalancesService(
            ExpenseRepositoryPort expenseRepository,
            GroupRepositoryPort groupRepository,
            SettlementRepositoryPort settlementRepository,
            DebtSimplificationService debtSimplificationService) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.settlementRepository = settlementRepository;
        this.debtSimplificationService = debtSimplificationService;
    }

    @Override
    public List<Transfer> calculateBalances(GroupId groupId) {
        log.info("Calculating balances for group {}", groupId);

        if (groupRepository.findById(groupId).isEmpty()) {
            throw new GroupNotFoundException(groupId);
        }

        List<Expense> expenses =
                expenseRepository.findAllByGroupId(groupId).stream()
                        .filter(e -> !e.isSettled())
                        .toList();

        List<Settlement> settlements = settlementRepository.findAllByGroupId(groupId);

        List<Transfer> transfers =
                debtSimplificationService.simplifyWithSettlements(expenses, settlements);

        log.info(
                "Calculated {} transfers for group {} (expenses={}, settlements={})",
                transfers.size(),
                groupId,
                expenses.size(),
                settlements.size());
        return transfers;
    }
}
