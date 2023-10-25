package com.vassarlabs.gp.exception;

public class UpdateCacheFailureException extends RuntimeException{
    public UpdateCacheFailureException() { }

    public UpdateCacheFailureException(String message) {
        super(message);
    }

    public UpdateCacheFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}

