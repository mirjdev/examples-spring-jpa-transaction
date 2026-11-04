package com.mirjdev.examplesspring.service;

import com.mirjdev.examplesspring.dto.ClientFilter;
import com.mirjdev.examplesspring.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    Client create(Client client);

    Client findById(Long id);

    Page<Client> findAll(ClientFilter filter, Pageable pageable);

    Client update(Long id, ClientUpdateRequest request);

    void delete(Long id);
}
