package com.mirjdev.examplesspring.client.exception;

/**
 * Клиент с указанным идентификатором не найден.
 */
public class ClientNotFoundException extends RuntimeException {

    private final Long clientId;

    public ClientNotFoundException(Long clientId) {
        super("Client with id=%d not found".formatted(clientId));
        this.clientId = clientId;
    }

    public Long getClientId() {
        return clientId;
    }
}
