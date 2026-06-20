package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.service.Transfer;
import com.mt.expense.app.domain.vo.GroupId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Response DTO for group balance summary. */
public record BalanceSummaryResponse(
        UUID groupId, Instant calculatedAt, List<TransferResponse> transfers) {
    public static BalanceSummaryResponse of(GroupId groupId, List<Transfer> transfers) {
        return new BalanceSummaryResponse(
                groupId.value(),
                Instant.now(),
                transfers.stream().map(TransferResponse::from).toList());
    }
}
