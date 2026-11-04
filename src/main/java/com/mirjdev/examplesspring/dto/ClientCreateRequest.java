package com.mirjdev.examplesspring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientCreateRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 255) String fio,
        @NotBlank @Size(max = 32)
        @Pattern(regexp = "\\+?[0-9]{7,15}", message = "must contain 7 to 15 digits with an optional leading +")
        String phone
) {
}
