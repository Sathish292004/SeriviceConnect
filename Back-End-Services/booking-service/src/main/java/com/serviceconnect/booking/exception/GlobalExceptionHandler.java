package com.serviceconnect.booking.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // ============================================================
    // RESPONSE STATUS EXCEPTION
    // ============================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException exception) {

        return buildResponse(
                exception.getStatusCode().value(),
                exception.getReason()
        );
    }


    // ============================================================
    // VALIDATION ERROR
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                OffsetDateTime.now()
        );

        response.put(
                "status",
                HttpStatus.BAD_REQUEST.value()
        );

        response.put(
                "error",
                "Validation failed"
        );

        response.put(
                "errors",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    // ============================================================
    // CONSTRAINT VIOLATION
    // ============================================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );
    }


    // ============================================================
    // ILLEGAL ARGUMENT
    // ============================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );
    }


    // ============================================================
    // ILLEGAL STATE
    // ============================================================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException exception) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage()
        );
    }


    // ============================================================
    // GENERIC EXCEPTION
    // ============================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception exception) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred"
        );
    }


    // ============================================================
    // BUILD RESPONSE
    // ============================================================

    private ResponseEntity<Map<String, Object>> buildResponse(
            int status,
            String message) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "timestamp",
                OffsetDateTime.now()
        );

        response.put(
                "status",
                status
        );

        response.put(
                "message",
                message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}