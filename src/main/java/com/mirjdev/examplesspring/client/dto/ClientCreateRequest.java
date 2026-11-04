package com.mirjdev.examplesspring.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание клиента.
 */
@Schema(description = "Данные для создания клиента")
public record ClientCreateRequest(

        @Schema(description = "Email клиента", example = "ivan.petrov@example.com", maxLength = 255)
        @NotBlank(message = "email is required")
        @Email(message = "email must be a well-formed email address")
        @Size(max = 255, message = "email must not exceed 255 characters")
        String email,

        @Schema(description = "ФИО клиента", example = "Иванов Иван Иванович", maxLength = 255)
        @NotBlank(message = "fio is required")
        @Size(max = 255, message = "fio must not exceed 255 characters")
        String fio,

        @Schema(description = "Телефон в формате +7XXXXXXXXXX", example = "+79991234567", maxLength = 32)
        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "phone must contain 10 to 15 digits with an optional leading +")
        @Size(max = 32, message = "phone must not exceed 32 characters")
        String phone
) {
}
