package com.mt.expense.app.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/** Standard error response envelope. */
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
        int status,
        String error,
        String message,
        String path) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path);
    }
}
