package com.mirjdev.examplesspring.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Представление клиента в REST API.
 */
@Schema(description = "Клиент")
public record ClientResponse(

        @Schema(description = "Идентификатор", example = "1")
        Long id,

        @Schema(description = "Email", example = "ivan.petrov@example.com")
        String email,

        @Schema(description = "ФИО", example = "Иванов Иван Иванович")
        String fio,

        @Schema(description = "Телефон", example = "+79991234567")
        String phone,

        @Schema(description = "Дата и время создания", example = "2026-11-04T14:16:17Z")
        Instant createdAt,

        @Schema(description = "Версия записи", example = "0")
        Long version
) {
}
