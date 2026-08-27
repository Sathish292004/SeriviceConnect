package com.serviceconnect.catalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // VALIDATION ERRORS
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .badRequest()
                .body(errors);
    }


    // ============================================================
    // RESPONSE STATUS EXCEPTION
    // ============================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(
            ResponseStatusException exception) {

        Map<String, String> response = new HashMap<>();

        response.put(
                "error",
                exception.getReason()
        );

        response.put(
                "status",
                String.valueOf(
                        exception.getStatusCode().value()
                )
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(response);
    }


    // ============================================================
    // GENERAL EXCEPTION
    // ============================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(
            Exception exception) {

        Map<String, String> response = new HashMap<>();

        response.put(
                "error",
                "Internal server error"
        );

        response.put(
                "status",
                String.valueOf(
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}