package com.mirjdev.examplesspring.exception;

public class ClientConflictException extends RuntimeException {

    public ClientConflictException(String message) {
        super(message);
    }
}
