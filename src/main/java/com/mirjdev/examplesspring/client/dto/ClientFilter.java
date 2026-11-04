package com.mirjdev.examplesspring.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

/**
 * Фильтр для поиска клиентов.
 *
 * <p>Все условия объединяются через AND. Строковые условия ищут подстроку
 * без учёта регистра, {@code query} — сразу по трём полям: {@code fio},
 * {@code email}, {@code phone}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Фильтр поиска клиентов")
public class ClientFilter {

    @Schema(description = "Поиск по fio, email и phone (без учёта регистра)", example = "ivan")
    @Size(max = 255, message = "query must not exceed 255 characters")
    private String query;

    @Schema(description = "Фильтр по email (подстрока, без учёта регистра)", example = "example.com")
    @Size(max = 255, message = "email must not exceed 255 characters")
    private String email;

    @Schema(description = "Фильтр по ФИО (подстрока, без учёта регистра)", example = "Иванов")
    @Size(max = 255, message = "fio must not exceed 255 characters")
    private String fio;

    @Schema(description = "Фильтр по телефону (подстрока)", example = "999")
    @Size(max = 32, message = "phone must not exceed 32 characters")
    private String phone;

    @Schema(description = "Создан не раньше (включительно), ISO-8601", example = "2026-01-01T00:00:00Z")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant createdFrom;

    @Schema(description = "Создан не позже (включительно), ISO-8601", example = "2026-12-31T23:59:59Z")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant createdTo;
}
