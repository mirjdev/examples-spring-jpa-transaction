package com.mirjdev.examplesspring.client;

import com.mirjdev.examplesspring.client.dto.ClientFilter;
import com.mirjdev.examplesspring.client.exception.ClientNotFoundException;
import com.mirjdev.examplesspring.client.exception.ClientVersionConflictException;
import com.mirjdev.examplesspring.client.exception.EmailAlreadyExistsException;
import com.mirjdev.examplesspring.client.exception.InvalidClientRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Реализация сервиса клиентов.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "email", "fio", "phone", "createdAt", "version");

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public Client create(Client client) {
        normalize(client);

        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new EmailAlreadyExistsException(client.getEmail());
        }

        return clientRepository.saveAndFlush(client);
    }

    @Override
    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Override
    public Page<Client> findAll(ClientFilter filter, Pageable pageable) {
        Objects.requireNonNull(filter, "filter must not be null");
        Objects.requireNonNull(pageable, "pageable must not be null");

        validateDateRange(filter);
        validateSort(pageable.getSort());

        return clientRepository.findAll(ClientSpecifications.matching(filter), pageable);
    }

    @Override
    @Transactional
    public Client update(Long id, Client client) {
        Client existing = getById(id);

        if (!Objects.equals(client.getVersion(), existing.getVersion())) {
            throw new ClientVersionConflictException(id, client.getVersion(), existing.getVersion());
        }

        normalize(client);

        if (!client.getEmail().equals(existing.getEmail())
                && clientRepository.existsByEmailAndIdNot(client.getEmail(), id)) {
            throw new EmailAlreadyExistsException(client.getEmail());
        }

        clientMapper.partialUpdate(client, existing);
        return clientRepository.saveAndFlush(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Client client = getById(id);
        clientRepository.delete(client);
    }

    private void validateDateRange(ClientFilter filter) {
        if (filter.getCreatedFrom() != null
                && filter.getCreatedTo() != null
                && filter.getCreatedFrom().isAfter(filter.getCreatedTo())) {
            throw new InvalidClientRequestException(
                    "createdFrom must be before or equal to createdTo");
        }
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new InvalidClientRequestException(
                        "Unsupported sort property '%s'. Allowed values: %s"
                                .formatted(order.getProperty(), ALLOWED_SORT_FIELDS));
            }
        }
    }

    private void normalize(Client client) {
        if (StringUtils.isAnyBlank(client.getEmail(), client.getFio(), client.getPhone())) {
            throw new InvalidClientRequestException("email, fio and phone are required");
        }
        client.setEmail(client.getEmail().trim().toLowerCase(Locale.ROOT));
        client.setFio(client.getFio().trim());
        client.setPhone(client.getPhone().trim());
    }
}
