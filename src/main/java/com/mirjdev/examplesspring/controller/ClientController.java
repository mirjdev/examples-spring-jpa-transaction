package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.dto.ClientCreateRequest;
import com.mirjdev.examplesspring.dto.ClientFilter;
import com.mirjdev.examplesspring.dto.ClientPageResponse;
import com.mirjdev.examplesspring.dto.ClientResponse;
import com.mirjdev.examplesspring.dto.ClientUpdateRequest;
import com.mirjdev.examplesspring.mapper.ClientMapper;
import com.mirjdev.examplesspring.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @GetMapping
    public ClientPageResponse findAll(
            @Valid @ParameterObject @ModelAttribute ClientFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable,
            // Validate raw values: the standard Pageable resolver silently corrects invalid bounds.
            @Parameter(hidden = true) @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(hidden = true) @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var clients = clientService.findAll(filter, PageRequest.of(page, size, pageable.getSort()));
        return new ClientPageResponse(clients.map(clientMapper::toResponse).getContent(),
                clients.getNumber(), clients.getSize(), clients.getTotalElements(), clients.getTotalPages());
    }

    @GetMapping("/{id}")
    public ClientResponse findById(@PathVariable @Positive Long id) {
        return clientMapper.toResponse(clientService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        var client = clientService.create(clientMapper.toEntity(request));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(client.getId()).toUri();
        return ResponseEntity.created(location).body(clientMapper.toResponse(client));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Частично обновить клиента",
            description = "Поля с null или отсутствующие поля сохраняют прежние значения. Обязательна текущая version.")
    public ClientResponse update(@PathVariable @Positive Long id,
                                 @Valid @RequestBody ClientUpdateRequest request) {
        return clientMapper.toResponse(clientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
