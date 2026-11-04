package com.mirjdev.examplesspring.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

public record ClientFilter(
        @Positive Long id,
        @Size(max = 254) String email,
        @Size(max = 255) String fio,
        @Size(max = 32) String phone,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
        @PositiveOrZero Long version
) {
}
