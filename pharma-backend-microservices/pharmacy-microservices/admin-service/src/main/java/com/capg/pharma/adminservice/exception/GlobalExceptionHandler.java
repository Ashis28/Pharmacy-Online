package com.capg.pharma.adminservice.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Admin Service.
 * Handles domain exceptions and Feign client errors from downstream services.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Handles admin business rule violations — returns 400. */
    @ExceptionHandler(AdminServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAdminService(AdminServiceException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Handles Feign 404 from downstream services — returns 404. */
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleFeignNotFound(FeignException.NotFound ex) {
        return buildError(HttpStatus.NOT_FOUND, "Resource not found in downstream service");
    }

    /** Handles Feign 401 from downstream services — returns 401. */
    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<Map<String, Object>> handleFeignUnauthorized(FeignException.Unauthorized ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Unauthorized access to downstream service");
    }

    /** Handles other Feign errors (service unavailable) — returns 503. */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeign(FeignException ex) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, "Downstream service unavailable");
    }

    /** Handles insufficient permissions — returns 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Access denied: ADMIN role required");
    }

    /** Handles Bean Validation failures — returns 400 with field-level errors. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Catch-all handler — returns 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
