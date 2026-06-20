package com.mt.expense.app.application.exception;

/** Thrown when attempting to create a user that already exists. */
public final class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String message) {
        super(message, "USER_ALREADY_EXISTS");
    }
}
