package com.mirjdev.examplesspring.dto;

import java.util.List;

/** Stable API contract independent of Spring Data's Page serialization. */
public record ClientPageResponse(
        List<ClientResponse> content, int page, int size, long totalElements, int totalPages
) {
}
