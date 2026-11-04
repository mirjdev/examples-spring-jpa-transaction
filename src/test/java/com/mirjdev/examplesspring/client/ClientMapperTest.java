package com.mirjdev.examplesspring.client;

import com.mirjdev.examplesspring.client.dto.ClientCreateRequest;
import com.mirjdev.examplesspring.client.dto.ClientResponse;
import com.mirjdev.examplesspring.client.dto.ClientUpdateRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ClientMapperTest {

    private final ClientMapper mapper = Mappers.getMapper(ClientMapper.class);

    @Test
    void toEntity_fromCreateRequest_ignoresTechnicalFields() {
        ClientCreateRequest request = new ClientCreateRequest(
                "ivan@example.com", "Ivan Petrov", "+79991234567");

        Client client = mapper.toEntity(request);

        assertThat(client.getEmail()).isEqualTo("ivan@example.com");
        assertThat(client.getFio()).isEqualTo("Ivan Petrov");
        assertThat(client.getPhone()).isEqualTo("+79991234567");
        assertThat(client.getId()).isNull();
        assertThat(client.getVersion()).isNull();
        assertThat(client.getCreatedAt()).isNull();
    }

    @Test
    void toEntity_fromUpdateRequest_mapsVersion() {
        ClientUpdateRequest request = new ClientUpdateRequest(
                "ivan@example.com", "Ivan Petrov", "+79991234567", 3L);

        Client client = mapper.toEntity(request);

        assertThat(client.getEmail()).isEqualTo("ivan@example.com");
        assertThat(client.getVersion()).isEqualTo(3L);
        assertThat(client.getId()).isNull();
        assertThat(client.getCreatedAt()).isNull();
    }

    @Test
    void toResponse_mapsAllFields() {
        Instant createdAt = Instant.parse("2026-11-04T12:00:00Z");
        Client client = Client.builder()
                .id(42L)
                .email("ivan@example.com")
                .fio("Ivan Petrov")
                .phone("+79991234567")
                .createdAt(createdAt)
                .version(2L)
                .build();

        ClientResponse response = mapper.toResponse(client);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.email()).isEqualTo("ivan@example.com");
        assertThat(response.fio()).isEqualTo("Ivan Petrov");
        assertThat(response.phone()).isEqualTo("+79991234567");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.version()).isEqualTo(2L);
    }

    @Test
    void partialUpdate_setsNullValuesAndPreservesTechnicalFields() {
        Instant createdAt = Instant.parse("2026-11-04T12:00:00Z");
        Client target = Client.builder()
                .id(42L)
                .email("old@example.com")
                .fio("Old Name")
                .phone("+79990000000")
                .createdAt(createdAt)
                .version(5L)
                .build();
        Client source = Client.builder()
                .email("new@example.com")
                .fio(null)
                .phone(null)
                .version(999L)
                .build();

        mapper.partialUpdate(source, target);

        assertThat(target.getEmail()).isEqualTo("new@example.com");
        assertThat(target.getFio()).isNull();
        assertThat(target.getPhone()).isNull();
        assertThat(target.getId()).isEqualTo(42L);
        assertThat(target.getVersion()).isEqualTo(5L);
        assertThat(target.getCreatedAt()).isEqualTo(createdAt);
    }
}
