package com.mt.expense.app.infrastructure.web.dto.response;

import com.mt.expense.app.domain.service.Transfer;

/** Response DTO for a single transfer between users. */
public record TransferResponse(UserSummary from, UserSummary to, MoneyDto amount) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                new UserSummary(transfer.from().value(), null),
                new UserSummary(transfer.to().value(), null),
                new MoneyDto(
                        transfer.amount().amount(),
                        transfer.amount().currency().getCurrencyCode()));
    }
}
