package com.mt.expense.app.application.port.out;

import com.mt.expense.app.domain.model.Settlement;
import com.mt.expense.app.domain.vo.GroupId;
import java.util.List;

/** Outbound Port (Secondary) — Repository interface for settlement persistence. */
public interface SettlementRepositoryPort {

    Settlement save(Settlement settlement);

    List<Settlement> findAllByGroupId(GroupId groupId);
}
