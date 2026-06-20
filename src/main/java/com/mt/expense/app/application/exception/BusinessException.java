package com.mt.expense.app.application.exception;

/** Base exception for all business errors in the application layer. */
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;

    protected BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
