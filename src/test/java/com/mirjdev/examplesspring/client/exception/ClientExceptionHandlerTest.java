package com.mirjdev.examplesspring.client.exception;

import com.mirjdev.examplesspring.client.dto.ClientCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClientExceptionHandlerTest {

    private final ClientExceptionHandler handler = new ClientExceptionHandler();

    @Test
    void dataIntegrityViolation_returnsConflict() {
        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate email"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Data conflict");
    }

    @Test
    void optimisticLockingFailure_returnsConflict() {
        ResponseEntity<ProblemDetail> response = handler.handleOptimisticLock(
                new OptimisticLockingFailureException("stale client"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Concurrent modification");
    }

    @Test
    void constraintViolation_returnsBadRequestWithErrors() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<ClientCreateRequest>> violations = validator.validate(
                new ClientCreateRequest("bad-email", "", "123"));

        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolation(
                new ConstraintViolationException(violations));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Validation failed");
        assertThat(response.getBody().getProperties()).containsKey("errors");
    }
}
