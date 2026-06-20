package com.mt.expense.app.application.exception;

/** Thrown when authentication fails. */
public final class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", cause);
    }
}
