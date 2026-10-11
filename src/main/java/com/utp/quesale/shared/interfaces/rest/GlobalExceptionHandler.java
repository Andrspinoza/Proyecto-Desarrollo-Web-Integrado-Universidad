package com.utp.quesale.shared.interfaces.rest;

import com.utp.quesale.shared.domain.exceptions.BusinessRuleViolationException;
import com.utp.quesale.shared.domain.exceptions.ForbiddenOperationException;
import com.utp.quesale.shared.domain.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

// Traduce las excepciones del dominio a respuestas HTTP con un formato único
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResource> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResource> handleForbidden(ForbiddenOperationException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler({BusinessRuleViolationException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResource> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ErrorResource> handleDataConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The operation conflicts with existing data", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResource> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ErrorResource> handleMalformed(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResource> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResource body = new ErrorResource(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), Instant.now());
        return ResponseEntity.status(status).body(body);
    }
}
