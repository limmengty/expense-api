package com.mt.expense.app.infrastructure.web.exception;

import com.mt.expense.app.application.exception.*;
import com.mt.expense.app.infrastructure.web.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SplitMismatchException.class)
    public ResponseEntity<ErrorResponse> handleSplitMismatch(
            SplitMismatchException ex, HttpServletRequest request) {
        return ResponseEntity.status(422)
                .body(
                        ErrorResponse.of(
                                422, ex.errorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NotGroupMemberException.class)
    public ResponseEntity<ErrorResponse> handleNotGroupMember(
            NotGroupMemberException ex, HttpServletRequest request) {
        return ResponseEntity.status(403)
                .body(
                        ErrorResponse.of(
                                403, ex.errorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExpenseNotFound(
            ExpenseNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(404)
                .body(
                        ErrorResponse.of(
                                404, ex.errorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGroupNotFound(
            GroupNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(404)
                .body(
                        ErrorResponse.of(
                                404, ex.errorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AlreadySettledException.class)
    public ResponseEntity<ErrorResponse> handleAlreadySettled(
            AlreadySettledException ex, HttpServletRequest request) {
        return ResponseEntity.status(409)
                .body(
                        ErrorResponse.of(
                                409, ex.errorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(403)
                .body(
                        ErrorResponse.of(
                                403, "ACCESS_DENIED", "Access denied", request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity.status(401)
                .body(
                        ErrorResponse.of(
                                401, ex.errorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(409)
                .body(
                        ErrorResponse.of(
                                409, ex.errorCode(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(
            SecurityException ex, HttpServletRequest request) {
        return ResponseEntity.status(403)
                .body(
                        ErrorResponse.of(
                                403, "ACCESS_DENIED", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getDefaultMessage())
                        .collect(Collectors.joining(","));
        return ResponseEntity.status(400)
                .body(ErrorResponse.of(400, "VALIDATION_ERROR", message, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.status(400)
                .body(
                        ErrorResponse.of(
                                400, "BAD_REQUEST", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(500)
                .body(
                        ErrorResponse.of(
                                500,
                                "INTERNAL_ERROR",
                                "An unexpected error occurred",
                                request.getRequestURI()));
    }
}
