package com.vassarlabs.gp.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() { }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
