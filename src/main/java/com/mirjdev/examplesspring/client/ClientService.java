package com.mirjdev.examplesspring.client;

import com.mirjdev.examplesspring.client.dto.ClientFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Контракт сервиса клиентов.
 */
public interface ClientService {

    Client create(Client client);

    Client getById(Long id);

    Page<Client> findAll(ClientFilter filter, Pageable pageable);

    Client update(Long id, Client client);

    void delete(Long id);
}
