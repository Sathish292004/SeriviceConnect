package com.serviceconnect.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // EMAIL ALREADY EXISTS
    // ============================================================

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(
            EmailAlreadyExistsException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Email Already Registered");
        problem.setDetail(exception.getMessage());
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // PHONE ALREADY EXISTS
    // ============================================================

    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ProblemDetail handlePhoneAlreadyExists(
            PhoneAlreadyExistsException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Phone Number Already Registered");
        problem.setDetail(exception.getMessage());
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // VALIDATION ERROR
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Validation Failed");
        problem.setDetail("Request validation failed");
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

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

        problem.setProperty(
                "errors",
                errors
        );

        return problem;
    }


    // ============================================================
    // CONSTRAINT VIOLATION
    // ============================================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Validation Failed");
        problem.setDetail("Request validation failed");
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // BAD CREDENTIALS
    // ============================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problem.setTitle("Authentication Failed");
        problem.setDetail("Invalid email or password");
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // RESPONSE STATUS EXCEPTION
    // ============================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(
                        exception.getStatusCode()
                );

        problem.setTitle(
                exception.getStatusCode().toString()
        );

        problem.setDetail(
                exception.getReason() == null
                        ? "Request could not be processed"
                        : exception.getReason()
        );

        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // MALFORMED JSON / INVALID REQUEST BODY
    // ============================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid Request Body");
        problem.setDetail("Malformed or invalid request body");
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // MISSING REQUEST HEADER
    // ============================================================

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Missing Request Header");
        problem.setDetail(
                "Required request header is missing: "
                        + exception.getHeaderName()
        );
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // HTTP METHOD NOT SUPPORTED
    // ============================================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(
                        HttpStatus.METHOD_NOT_ALLOWED
                );

        problem.setTitle("Method Not Allowed");
        problem.setDetail(
                "HTTP method is not supported for this endpoint"
        );
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // MEDIA TYPE NOT SUPPORTED
    // ============================================================

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE
                );

        problem.setTitle("Unsupported Media Type");
        problem.setDetail(
                "Request content type is not supported"
        );
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // DATA INTEGRITY VIOLATION
    // ============================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Data Conflict");
        problem.setDetail(
                "Request conflicts with existing data"
        );
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // ILLEGAL ARGUMENT
    // ============================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid Request");
        problem.setDetail(
                exception.getMessage() == null
                        ? "Invalid request"
                        : exception.getMessage()
        );
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // ILLEGAL STATE
    // ============================================================

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Invalid Request State");
        problem.setDetail(
                exception.getMessage() == null
                        ? "Request cannot be processed in the current state"
                        : exception.getMessage()
        );
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }


    // ============================================================
    // GENERIC EXCEPTION
    // ============================================================

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(
            Exception exception,
            HttpServletRequest request) {

        ProblemDetail problem =
                ProblemDetail.forStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR
                );

        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred");
        problem.setInstance(
                java.net.URI.create(request.getRequestURI())
        );

        return problem;
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ProblemDetail> handleSessionExpired(
            SessionExpiredException ex,
            HttpServletRequest request) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        problemDetail.setTitle("Session Expired");
        problemDetail.setDetail(
                "Your session has expired or is no longer valid. Please authenticate again."
        );
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }
}