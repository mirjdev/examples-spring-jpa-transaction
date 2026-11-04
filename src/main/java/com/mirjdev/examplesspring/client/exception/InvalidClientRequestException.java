package com.mirjdev.examplesspring.client.exception;

/**
 * Некорректный запрос к API клиентов, не связанный с bean validation.
 */
public class InvalidClientRequestException extends RuntimeException {

    public InvalidClientRequestException(String message) {
        super(message);
    }
}
