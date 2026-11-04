package com.mirjdev.examplesspring.client.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Единый формат ошибок API клиентов (RFC 9457 Problem Details).
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.mirjdev.examplesspring.client")
public class ClientExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ClientNotFoundException exception) {
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "Client not found", exception.getMessage());
        problem.setProperty("clientId", exception.getClientId());
        return response(problem);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Email already exists", exception.getMessage());
        problem.setProperty("email", exception.getEmail());
        return response(problem);
    }

    @ExceptionHandler(ClientVersionConflictException.class)
    public ResponseEntity<ProblemDetail> handleVersionConflict(ClientVersionConflictException exception) {
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "Client version conflict", exception.getMessage());
        problem.setProperty("clientId", exception.getClientId());
        problem.setProperty("expectedVersion", exception.getExpectedVersion());
        problem.setProperty("actualVersion", exception.getActualVersion());
        return response(problem);
    }

    @ExceptionHandler(InvalidClientRequestException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRequest(InvalidClientRequestException exception) {
        return response(problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), defaultMessage(fieldError.getDefaultMessage()));
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed");
        problem.setProperty("errors", errors);
        return response(problem);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleMethodValidation(HandlerMethodValidationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ParameterValidationResult result : exception.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                for (FieldError fieldError : parameterErrors.getFieldErrors()) {
                    errors.putIfAbsent(fieldError.getField(), defaultMessage(fieldError.getDefaultMessage()));
                }
                for (ObjectError objectError : parameterErrors.getGlobalErrors()) {
                    errors.putIfAbsent("request", defaultMessage(objectError.getDefaultMessage()));
                }
            } else {
                String parameterName = result.getMethodParameter().getParameterName();
                String key = parameterName == null ? "request" : parameterName;
                result.getResolvableErrors().forEach(error ->
                        errors.putIfAbsent(key, defaultMessage(error.getDefaultMessage())));
            }
        }
        exception.getCrossParameterValidationResults().forEach(error ->
                errors.putIfAbsent("request", defaultMessage(error.getDefaultMessage())));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed");
        problem.setProperty("errors", errors);
        return response(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errors.putIfAbsent(violation.getPropertyPath().toString(), defaultMessage(violation.getMessage()));
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed");
        problem.setProperty("errors", errors);
        return response(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String detail = "Parameter '%s' has invalid value '%s'"
                .formatted(exception.getName(), exception.getValue());
        return response(problem(HttpStatus.BAD_REQUEST, "Invalid request", detail));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        log.debug("Malformed request body", exception);
        return response(problem(HttpStatus.BAD_REQUEST, "Malformed request", "Request body is missing or malformed"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation", exception);
        return response(problem(HttpStatus.CONFLICT, "Data conflict", "Request violates a database constraint"));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockingFailureException exception) {
        log.debug("Optimistic locking conflict", exception);
        return response(problem(
                HttpStatus.CONFLICT,
                "Concurrent modification",
                "Client was modified concurrently, reload it and retry"
        ));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }

    private static ResponseEntity<ProblemDetail> response(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private static String defaultMessage(String message) {
        return message == null || message.isBlank() ? "invalid value" : message;
    }
}
