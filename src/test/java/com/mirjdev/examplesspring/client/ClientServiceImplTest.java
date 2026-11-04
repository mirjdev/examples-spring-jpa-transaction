package com.mirjdev.examplesspring.client;

import com.mirjdev.examplesspring.client.dto.ClientFilter;
import com.mirjdev.examplesspring.client.exception.ClientNotFoundException;
import com.mirjdev.examplesspring.client.exception.ClientVersionConflictException;
import com.mirjdev.examplesspring.client.exception.EmailAlreadyExistsException;
import com.mirjdev.examplesspring.client.exception.InvalidClientRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void create_normalizesFieldsAndSaves() {
        when(clientRepository.saveAndFlush(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Client result = clientService.create(Client.builder()
                .email("  IVAN@Example.COM ")
                .fio("  Ivan Petrov  ")
                .phone("  +79991234567  ")
                .build());

        assertThat(result.getEmail()).isEqualTo("ivan@example.com");
        assertThat(result.getFio()).isEqualTo("Ivan Petrov");
        assertThat(result.getPhone()).isEqualTo("+79991234567");
        verify(clientRepository).existsByEmail("ivan@example.com");
        verify(clientRepository).saveAndFlush(result);
    }

    @Test
    void create_blankRequiredField_throwsBadRequest() {
        assertThatThrownBy(() -> clientService.create(Client.builder()
                .email(" ")
                .fio("Ivan Petrov")
                .phone("+79991234567")
                .build()))
                .isInstanceOf(InvalidClientRequestException.class)
                .hasMessageContaining("required");

        verifyNoInteractions(clientRepository);
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        when(clientRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(Client.builder()
                .email("dup@example.com")
                .fio("Dup")
                .phone("+79991234567")
                .build()))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("dup@example.com");

        verify(clientRepository, never()).saveAndFlush(any(Client.class));
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(clientRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getById(42L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_validFilterAndSort_callsRepository() {
        ClientFilter filter = new ClientFilter();
        filter.setFio("Ivan");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("fio"));
        when(clientRepository.findAll(ArgumentMatchers.<Specification<Client>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Client> result = clientService.findAll(filter, pageable);

        assertThat(result).isEmpty();
        verify(clientRepository).findAll(ArgumentMatchers.<Specification<Client>>any(), any(Pageable.class));
    }

    @Test
    void findAll_invalidDateRange_throwsBadRequest() {
        ClientFilter filter = new ClientFilter();
        filter.setCreatedFrom(Instant.parse("2100-01-01T00:00:00Z"));
        filter.setCreatedTo(Instant.parse("2000-01-01T00:00:00Z"));

        assertThatThrownBy(() -> clientService.findAll(filter, PageRequest.of(0, 10)))
                .isInstanceOf(InvalidClientRequestException.class)
                .hasMessageContaining("createdFrom");

        verifyNoInteractions(clientRepository);
    }

    @Test
    void findAll_invalidSort_throwsBadRequest() {
        ClientFilter filter = new ClientFilter();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("password"));

        assertThatThrownBy(() -> clientService.findAll(filter, pageable))
                .isInstanceOf(InvalidClientRequestException.class)
                .hasMessageContaining("Unsupported sort property");

        verifyNoInteractions(clientRepository);
    }

    @Test
    void update_versionMismatch_throwsConflict() {
        Client existing = existingClient(1L, "old@example.com", 3L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));

        Client patch = Client.builder()
                .email("new@example.com")
                .fio("New Name")
                .phone("+79991234567")
                .version(2L)
                .build();

        assertThatThrownBy(() -> clientService.update(1L, patch))
                .isInstanceOf(ClientVersionConflictException.class)
                .hasMessageContaining("version 3")
                .hasMessageContaining("version 2");

        verifyNoInteractions(clientMapper);
        verify(clientRepository, never()).saveAndFlush(any(Client.class));
    }

    @Test
    void update_nullVersion_throwsConflict() {
        Client existing = existingClient(1L, "old@example.com", 3L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));

        Client patch = Client.builder()
                .email("new@example.com")
                .fio("New Name")
                .phone("+79991234567")
                .build();

        assertThatThrownBy(() -> clientService.update(1L, patch))
                .isInstanceOf(ClientVersionConflictException.class);
    }

    @Test
    void update_success_normalizesFieldsAndSaves() {
        Client existing = existingClient(1L, "old@example.com", 3L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(clientRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(clientRepository.saveAndFlush(existing)).thenReturn(existing);
        doAnswer(invocation -> {
            Client source = invocation.getArgument(0);
            Client target = invocation.getArgument(1);
            target.setEmail(source.getEmail());
            target.setFio(source.getFio());
            target.setPhone(source.getPhone());
            return null;
        }).when(clientMapper).partialUpdate(any(Client.class), any(Client.class));

        Client patch = Client.builder()
                .email(" New@Example.com ")
                .fio(" New Name ")
                .phone(" +79991234567 ")
                .version(3L)
                .build();

        Client result = clientService.update(1L, patch);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getFio()).isEqualTo("New Name");
        assertThat(result.getPhone()).isEqualTo("+79991234567");
        verify(clientMapper).partialUpdate(patch, existing);
        verify(clientRepository).saveAndFlush(existing);
    }

    @Test
    void update_duplicateEmail_throwsConflict() {
        Client existing = existingClient(1L, "old@example.com", 3L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(clientRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

        Client patch = Client.builder()
                .email("taken@example.com")
                .fio("New Name")
                .phone("+79991234567")
                .version(3L)
                .build();

        assertThatThrownBy(() -> clientService.update(1L, patch))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verifyNoInteractions(clientMapper);
        verify(clientRepository, never()).saveAndFlush(any(Client.class));
    }

    @Test
    void delete_existingClient_deletes() {
        Client existing = existingClient(1L, "old@example.com", 3L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existing));

        clientService.delete(1L);

        verify(clientRepository).delete(existing);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(clientRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(42L))
                .isInstanceOf(ClientNotFoundException.class);

        verify(clientRepository, never()).delete(any(Client.class));
    }

    private static Client existingClient(Long id, String email, Long version) {
        return Client.builder()
                .id(id)
                .email(email)
                .fio("Old Name")
                .phone("+79990000000")
                .version(version)
                .build();
    }
}
