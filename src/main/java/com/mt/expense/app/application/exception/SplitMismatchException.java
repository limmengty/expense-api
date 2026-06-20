package com.mt.expense.app.application.exception;

import com.mt.expense.app.domain.vo.Money;

/** Thrown when the sum of split amounts does not equal the expense total. */
public final class SplitMismatchException extends BusinessException {

    private final Money sumOfSplits;
    private final Money totalAmount;

    public SplitMismatchException(Money sumOfSplits, Money totalAmount) {
        super(
                String.format(
                        "Split amounts (%s) do not equal expense total (%s)",
                        sumOfSplits, totalAmount),
                "SPLIT_MISMATCH");
        this.sumOfSplits = sumOfSplits;
        this.totalAmount = totalAmount;
    }

    public Money sumOfSplits() {
        return sumOfSplits;
    }

    public Money totalAmount() {
        return totalAmount;
    }
}
