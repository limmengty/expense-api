package com.mt.expense.app.application.port.in;

import com.mt.expense.app.application.command.RecordSettlementCommand;
import com.mt.expense.app.domain.model.Settlement;

/** Inbound Port (Primary) — Use Case for recording a cash settlement between two users. */
public interface RecordSettlementUseCase {

    /**
     * Records that the payer (from) has paid the payee (to) the given amount within a group. This
     * reduces the calculated debt between these two users for future balance computations.
     *
     * @param command the settlement details
     * @return the persisted settlement
     */
    Settlement record(RecordSettlementCommand command);
}
