package com.mirjdev.examplesspring.exception;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(Long id) {
        super("Client with id " + id + " was not found");
    }
}
