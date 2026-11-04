package com.mirjdev.examplesspring.client.exception;

/**
 * Email уже используется другим клиентом.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("Client with email '%s' already exists".formatted(email));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
