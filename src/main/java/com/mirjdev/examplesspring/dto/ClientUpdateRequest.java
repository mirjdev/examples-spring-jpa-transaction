package com.mirjdev.examplesspring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Null fields retain their current values; version is required to prevent lost updates. */
public record ClientUpdateRequest(
        @Email @Size(min = 1, max = 254) @Pattern(regexp = ".*\\S.*") String email,
        @Size(min = 1, max = 255) @Pattern(regexp = "(?s).*[^\\p{javaWhitespace}].*") String fio,
        @Size(max = 32)
        @Pattern(regexp = "\\+?[0-9]{7,15}", message = "must contain 7 to 15 digits with an optional leading +")
        String phone,
        @NotNull @PositiveOrZero Long version
) {
}
