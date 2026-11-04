package com.mirjdev.examplesspring.client.exception;

/**
 * Версия клиента, переданная в запросе, устарела.
 */
public class ClientVersionConflictException extends RuntimeException {

    private final Long clientId;
    private final Long expectedVersion;
    private final Long actualVersion;

    public ClientVersionConflictException(Long clientId, Long expectedVersion, Long actualVersion) {
        super("Client with id=%d has version %s, but version %s was supplied"
                .formatted(clientId, actualVersion, expectedVersion));
        this.clientId = clientId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }

    public Long getActualVersion() {
        return actualVersion;
    }
}
