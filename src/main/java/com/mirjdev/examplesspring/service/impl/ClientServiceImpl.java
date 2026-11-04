package com.mirjdev.examplesspring.service.impl;

import com.mirjdev.examplesspring.dto.ClientFilter;
import com.mirjdev.examplesspring.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.entity.Client;
import com.mirjdev.examplesspring.entity.repository.ClientRepository;
import com.mirjdev.examplesspring.entity.repository.ClientSpecifications;
import com.mirjdev.examplesspring.exception.ClientConflictException;
import com.mirjdev.examplesspring.exception.ClientNotFoundException;
import com.mirjdev.examplesspring.mapper.ClientMapper;
import com.mirjdev.examplesspring.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "email", "fio", "phone", "createdAt", "version");
    private static final int MAX_PAGE_SIZE = 100;

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public Client create(Client client) {
        normalize(client);
        return clientRepository.saveAndFlush(client);
    }

    @Override
    public Client findById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Override
    public Page<Client> findAll(ClientFilter filter, Pageable pageable) {
        if (filter.createdFrom() != null && filter.createdTo() != null
                && filter.createdFrom().isAfter(filter.createdTo())) {
            throw new IllegalArgumentException("createdFrom must not be after createdTo");
        }
        return clientRepository.findAll(ClientSpecifications.matching(filter), validatedPageable(pageable));
    }

    @Override
    @Transactional
    public Client update(Long id, ClientUpdateRequest request) {
        Client client = findById(id);
        if (!Objects.equals(client.getVersion(), request.version())) {
            throw new ClientConflictException("Client has been changed; reload the current version before updating");
        }
        clientMapper.partialUpdate(request, client);
        normalize(client);
        return clientRepository.saveAndFlush(client);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Client client = findById(id);
        clientRepository.delete(client);
        clientRepository.flush();
    }

    private static Pageable validatedPageable(Pageable pageable) {
        if (pageable.isUnpaged() || pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (pageable.getOffset() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Requested page exceeds the supported database offset");
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
            }
        }
        Sort sort = pageable.getSort();
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by("id"));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static void normalize(Client client) {
        client.setEmail(client.getEmail().strip().toLowerCase(Locale.ROOT));
        client.setFio(client.getFio().strip());
        client.setPhone(client.getPhone().strip());
    }
}
