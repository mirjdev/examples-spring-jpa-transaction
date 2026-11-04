package com.mirjdev.examplesspring.client;

import com.mirjdev.examplesspring.client.dto.ClientCreateRequest;
import com.mirjdev.examplesspring.client.dto.ClientFilter;
import com.mirjdev.examplesspring.client.dto.ClientResponse;
import com.mirjdev.examplesspring.client.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.client.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * CRUD REST API клиентов.
 */
@Tag(name = "Clients", description = "CRUD API для работы с клиентами")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @Operation(summary = "Создать клиента")
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        Client created = clientService.create(clientMapper.toEntity(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(clientMapper.toResponse(created));
    }

    @Operation(summary = "Получить клиента по идентификатору")
    @GetMapping("/{id}")
    public ClientResponse getById(@Parameter(description = "Идентификатор клиента")
                                  @PathVariable @Positive Long id) {
        return clientMapper.toResponse(clientService.getById(id));
    }

    @Operation(summary = "Получить страницу клиентов с фильтрацией")
    @GetMapping
    public PageResponse<ClientResponse> findAll(
            @ParameterObject
            @Valid
            @ModelAttribute ClientFilter filter,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return PageResponse.of(
                clientService.findAll(filter, pageable).map(clientMapper::toResponse)
        );
    }

    @Operation(summary = "Обновить клиента")
    @PutMapping("/{id}")
    public ClientResponse update(@Parameter(description = "Идентификатор клиента")
                                 @PathVariable @Positive Long id,
                                 @Valid @RequestBody ClientUpdateRequest request) {
        Client updated = clientService.update(id, clientMapper.toEntity(request));
        return clientMapper.toResponse(updated);
    }

    @Operation(summary = "Удалить клиента")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Идентификатор клиента")
                                       @PathVariable @Positive Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
