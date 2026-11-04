package com.mirjdev.examplesspring.service;

import com.mirjdev.examplesspring.dto.ClientFilter;
import com.mirjdev.examplesspring.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.entity.Client;
import com.mirjdev.examplesspring.entity.repository.ClientRepository;
import com.mirjdev.examplesspring.exception.ClientConflictException;
import com.mirjdev.examplesspring.exception.ClientNotFoundException;
import com.mirjdev.examplesspring.mapper.ClientMapper;
import com.mirjdev.examplesspring.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:15:30Z");
    private static final ClientFilter NO_FILTER = new ClientFilter(null, null, null, null, null, null, null);

    @Mock
    private ClientRepository repository;

    private ClientService service;

    @BeforeEach
    void setUp() {
        service = new ClientServiceImpl(repository, Mappers.getMapper(ClientMapper.class));
    }

    @Test
    void createsClientWithNormalizedContactDetails() {
        Client input = client();
        input.setId(null);
        input.setVersion(null);
        input.setCreatedAt(null);
        input.setEmail("  IVAN@EXAMPLE.COM  ");
        input.setFio("  Иван Иванов  ");
        input.setPhone("  +79991234567  ");
        when(repository.saveAndFlush(input)).thenAnswer(invocation -> {
            Client saved = invocation.getArgument(0);
            saved.setId(42L);
            saved.setVersion(0L);
            saved.setCreatedAt(CREATED_AT);
            return saved;
        });

        Client result = service.create(input);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getEmail()).isEqualTo("ivan@example.com");
        assertThat(result.getFio()).isEqualTo("Иван Иванов");
        assertThat(result.getPhone()).isEqualTo("+79991234567");
        assertThat(result.getVersion()).isZero();
        assertThat(result.getCreatedAt()).isEqualTo(CREATED_AT);
        verify(repository).saveAndFlush(input);
    }

    @Test
    void propagatesDatabaseConstraintFailureDuringCreate() {
        Client input = client();
        DataIntegrityViolationException failure = new DataIntegrityViolationException("uk_clients_email");
        when(repository.saveAndFlush(input)).thenThrow(failure);

        assertThatThrownBy(() -> service.create(input)).isSameAs(failure);
    }

    @Test
    void returnsExistingClient() {
        Client expected = client();
        when(repository.findById(42L)).thenReturn(Optional.of(expected));

        assertThat(service.findById(42L)).isSameAs(expected);
    }

    @Test
    void reportsMissingClient() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L)).isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void partiallyUpdatesClientAndPreservesServerManagedFields() {
        Client existing = client();
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        Client updated = service.update(42L, new ClientUpdateRequest("  NEW@EXAMPLE.COM  ", "  Пётр Петров  ", null, 3L));

        assertThat(updated).isSameAs(existing);
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getFio()).isEqualTo("Пётр Петров");
        assertThat(updated.getPhone()).isEqualTo("+79991234567");
        assertThat(updated.getId()).isEqualTo(42L);
        assertThat(updated.getVersion()).isEqualTo(3L);
        assertThat(updated.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void updatesOnlyPhoneWhenOtherFieldsAreOmitted() {
        Client existing = client();
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        Client updated = service.update(42L, new ClientUpdateRequest(null, null, "  +78881234567  ", 3L));

        assertThat(updated.getEmail()).isEqualTo("ivan@example.com");
        assertThat(updated.getFio()).isEqualTo("Иван Иванов");
        assertThat(updated.getPhone()).isEqualTo("+78881234567");
    }

    @Test
    void rejectsStaleVersionBeforeChangingClient() {
        Client existing = client();
        when(repository.findById(42L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(42L, new ClientUpdateRequest("new@example.com", null, null, 2L)))
                .isInstanceOf(ClientConflictException.class);

        assertThat(existing.getEmail()).isEqualTo("ivan@example.com");
        verify(repository, never()).saveAndFlush(any(Client.class));
    }

    @Test
    void rejectsUpdateOfMissingClient() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L, new ClientUpdateRequest(null, null, "+78881234567", 3L)))
                .isInstanceOf(ClientNotFoundException.class);

        verify(repository, never()).saveAndFlush(any(Client.class));
    }

    @Test
    void deletesExistingClientAndFlushesBeforeReturning() {
        Client existing = client();
        when(repository.findById(42L)).thenReturn(Optional.of(existing));

        service.delete(42L);

        var order = inOrder(repository);
        order.verify(repository).findById(42L);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
    }

    @Test
    void rejectsDeletionOfMissingClient() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L)).isInstanceOf(ClientNotFoundException.class);

        verify(repository, never()).delete(any(Client.class));
        verify(repository, never()).flush();
    }

    @Test
    void preservesPageMetadataAndAddsIdAsStableSortTieBreaker() {
        Pageable input = PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "fio"));
        Page<Client> expected = new PageImpl<>(List.of(client()), input, 21);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        assertThat(service.findAll(NO_FILTER, input)).isSameAs(expected);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageable.getValue().getSort().getOrderFor("fio").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getValue().getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    void preservesExplicitIdSortWithoutDuplicatingIt() {
        Pageable input = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id"));
        when(repository.findAll(any(Specification.class), eq(input))).thenReturn(Page.empty(input));

        assertThat(service.findAll(NO_FILTER, input)).isEmpty();

        verify(repository).findAll(any(Specification.class), eq(input));
    }

    @Test
    void rejectsUnknownSortProperty() {
        Pageable input = PageRequest.of(0, 20, Sort.by("password"));

        assertThatThrownBy(() -> service.findAll(NO_FILTER, input)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsPageLargerThanMaximum() {
        assertThatThrownBy(() -> service.findAll(NO_FILTER, PageRequest.of(0, 101)))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsUnboundedListRequests() {
        assertThatThrownBy(() -> service.findAll(NO_FILTER, Pageable.unpaged()))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsPageOffsetBeyondJpaIntegerLimit() {
        assertThatThrownBy(() -> service.findAll(NO_FILTER, PageRequest.of(Integer.MAX_VALUE, 100)))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvertedCreationDateRange() {
        ClientFilter filter = new ClientFilter(null, null, null, null, CREATED_AT.plusSeconds(1), CREATED_AT, null);

        assertThatThrownBy(() -> service.findAll(filter, PageRequest.of(0, 20)))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    private static Client client() {
        Client client = new Client();
        client.setId(42L);
        client.setEmail("ivan@example.com");
        client.setFio("Иван Иванов");
        client.setPhone("+79991234567");
        client.setCreatedAt(CREATED_AT);
        client.setVersion(3L);
        return client;
    }
}
