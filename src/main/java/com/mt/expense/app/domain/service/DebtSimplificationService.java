package com.mt.expense.app.domain.service;

import com.mt.expense.app.domain.model.Expense;
import com.mt.expense.app.domain.model.ExpenseSplit;
import com.mt.expense.app.domain.model.Settlement;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.UserId;
import java.math.BigDecimal;
import java.util.*;

@org.springframework.stereotype.Service
public final class DebtSimplificationService {

    public List<Transfer> simplify(List<Expense> expenses) {
        return simplifyWithSettlements(expenses, Collections.emptyList());
    }

    /**
     * Computes minimized transfers taking into account both unsettled expenses and recorded cash
     * settlements. Each settlement reduces the outstanding debt between two users before the
     * simplification algorithm runs.
     */
    public List<Transfer> simplifyWithSettlements(
            List<Expense> expenses, List<Settlement> settlements) {
        Map<UserId, BigDecimal> netBalance =
                (expenses == null || expenses.isEmpty())
                        ? new HashMap<>()
                        : calculateNetBalances(expenses);

        // Apply recorded settlements: payer's credit increases, payee's credit decreases
        for (Settlement s : settlements) {
            netBalance.merge(s.from(), s.amount().amount(), BigDecimal::add);
            netBalance.merge(s.to(), s.amount().amount().negate(), BigDecimal::add);
        }

        netBalance.entrySet().removeIf(e -> e.getValue().compareTo(BigDecimal.ZERO) == 0);

        if (netBalance.isEmpty()) {
            return Collections.emptyList();
        }

        return buildMinimizedTransfers(netBalance);
    }

    private List<Transfer> buildMinimizedTransfers(Map<UserId, BigDecimal> netBalance) {
        List<Map.Entry<UserId, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<UserId, BigDecimal>> debtors = new ArrayList<>();

        for (Map.Entry<UserId, BigDecimal> entry : netBalance.entrySet()) {
            int cmp = entry.getValue().compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            } else if (cmp < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().abs()));
            }
        }

        creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        debtors.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Transfer> transfers = new ArrayList<>();
        java.util.Currency currency = java.util.Currency.getInstance("USD");

        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            Map.Entry<UserId, BigDecimal> creditor = creditors.get(i);
            Map.Entry<UserId, BigDecimal> debtor = debtors.get(j);

            BigDecimal creditAmt = creditor.getValue();
            BigDecimal debtAmt = debtor.getValue();
            BigDecimal settleAmt = creditAmt.min(debtAmt);

            if (settleAmt.compareTo(BigDecimal.ZERO) > 0) {
                transfers.add(
                        new Transfer(
                                debtor.getKey(),
                                creditor.getKey(),
                                Money.of(settleAmt, currency.getCurrencyCode())));
            }

            BigDecimal newCredit = creditAmt.subtract(settleAmt);
            BigDecimal newDebt = debtAmt.subtract(settleAmt);

            creditor.setValue(newCredit);
            debtor.setValue(newDebt);

            if (newCredit.compareTo(BigDecimal.ZERO) == 0) i++;
            if (newDebt.compareTo(BigDecimal.ZERO) == 0) j++;
        }
        return transfers;
    }

    Map<UserId, BigDecimal> calculateNetBalances(List<Expense> expenses) {
        Map<UserId, BigDecimal> balances = new HashMap<>();
        for (Expense expense : expenses) {
            UserId payerId = expense.payerId();
            Money amount = expense.amount();
            balances.merge(payerId, amount.amount(), BigDecimal::add);
            for (ExpenseSplit split : expense.splits()) {
                UserId participantId = split.userId();
                BigDecimal owedAmount = split.shareAmount().amount();
                balances.merge(participantId, owedAmount.negate(), BigDecimal::add);
            }
        }
        return balances;
    }
}
