package com.webapp.exception;

public class FileDeletionException extends RuntimeException {
    public FileDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
