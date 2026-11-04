package com.mirjdev.examplesspring.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;

/**
 * Стабильный контракт страницы.
 *
 * <p>Не отдаём наружу Spring-овский {@code Page}, чтобы не привязывать
 * публичное API к внутренней структуре {@code PageImpl}.</p>
 */
@Schema(description = "Страница результатов")
public record PageResponse<T>(

        @Schema(description = "Элементы страницы")
        List<T> content,

        @Schema(description = "Номер страницы (с нуля)", example = "0")
        int page,

        @Schema(description = "Размер страницы", example = "20")
        int size,

        @Schema(description = "Всего элементов", example = "42")
        long totalElements,

        @Schema(description = "Всего страниц", example = "3")
        int totalPages,

        @Schema(description = "Признак первой страницы", example = "true")
        boolean first,

        @Schema(description = "Признак последней страницы", example = "false")
        boolean last
) {

    public PageResponse {
        content = List.copyOf(content);
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        Objects.requireNonNull(page, "page must not be null");
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
