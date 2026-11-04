package com.mirjdev.examplesspring.dto;

import java.time.Instant;

public record ClientResponse(Long id, String email, String fio, String phone, Instant createdAt, Long version) {
}
