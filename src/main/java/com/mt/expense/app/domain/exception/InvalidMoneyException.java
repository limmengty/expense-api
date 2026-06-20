package com.mt.expense.app.domain.exception;

/** Thrown when an invalid monetary amount is created (e.g., negative amount). */
public final class InvalidMoneyException extends RuntimeException {

    public InvalidMoneyException(String message) {
        super(message);
    }
}
