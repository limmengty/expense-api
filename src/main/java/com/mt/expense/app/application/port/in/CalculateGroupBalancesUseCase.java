package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.exception.GroupNotFoundException;
import com.mt.expense.app.domain.service.Transfer;
import com.mt.expense.app.domain.vo.GroupId;
import java.util.List;

/** Inbound Port (Primary) — Use Case for calculating group balances. */
public interface CalculateGroupBalancesUseCase {

    /**
     * Calculates the minimized list of transfers needed to settle all group expenses.
     *
     * @param groupId the group to calculate balances for
     * @return list of transfers representing minimum transactions needed
     * @throws GroupNotFoundException if the group doesn't exist
     */
    List<Transfer> calculateBalances(GroupId groupId);
}
