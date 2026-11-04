package com.mirjdev.examplesspring.mapper;

import com.mirjdev.examplesspring.dto.ClientCreateRequest;
import com.mirjdev.examplesspring.dto.ClientResponse;
import com.mirjdev.examplesspring.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.entity.Client;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ClientMapperTest {

    private final ClientMapper mapper = Mappers.getMapper(ClientMapper.class);

    @Test
    void mapsCreateRequestWithoutSettingServerManagedFields() {
        ClientCreateRequest request = new ClientCreateRequest("ivan@example.com", "Иван Иванов", "+79991234567");

        Client entity = mapper.toEntity(request);

        assertThat(entity.getEmail()).isEqualTo(request.email());
        assertThat(entity.getFio()).isEqualTo(request.fio());
        assertThat(entity.getPhone()).isEqualTo(request.phone());
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getVersion()).isNull();
    }

    @Test
    void mapsAllResponseFields() {
        Client entity = client();

        ClientResponse response = mapper.toResponse(entity);

        assertThat(response).isEqualTo(new ClientResponse(42L, "ivan@example.com", "Иван Иванов", "+79991234567",
                Instant.parse("2026-10-01T10:15:30Z"), 3L));
    }

    @Test
    void updatesProvidedFieldsWithoutOverwritingIdentityTimestampOrVersion() {
        Client entity = client();

        Client updated = mapper.partialUpdate(new ClientUpdateRequest("new@example.com", "Пётр Петров", "+78881234567", 99L), entity);

        assertThat(updated).isSameAs(entity);
        assertThat(entity.getEmail()).isEqualTo("new@example.com");
        assertThat(entity.getFio()).isEqualTo("Пётр Петров");
        assertThat(entity.getPhone()).isEqualTo("+78881234567");
        assertThat(entity.getId()).isEqualTo(42L);
        assertThat(entity.getCreatedAt()).isEqualTo(Instant.parse("2026-10-01T10:15:30Z"));
        assertThat(entity.getVersion()).isEqualTo(3L);
    }

    @Test
    void preservesFieldsOmittedFromPartialUpdate() {
        Client entity = client();

        mapper.partialUpdate(new ClientUpdateRequest(null, null, null, 3L), entity);

        assertThat(mapper.toResponse(entity)).isEqualTo(new ClientResponse(42L, "ivan@example.com", "Иван Иванов", "+79991234567",
                Instant.parse("2026-10-01T10:15:30Z"), 3L));
    }

    @Test
    void handlesNullSources() {
        Client entity = client();

        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.partialUpdate(null, entity)).isSameAs(entity);
    }

    private static Client client() {
        Client entity = new Client();
        entity.setId(42L);
        entity.setEmail("ivan@example.com");
        entity.setFio("Иван Иванов");
        entity.setPhone("+79991234567");
        entity.setCreatedAt(Instant.parse("2026-10-01T10:15:30Z"));
        entity.setVersion(3L);
        return entity;
    }
}
