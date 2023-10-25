package com.vassarlabs.gp.exception;

/**
 * This custom exception can be used whenever empty strings (or) nulls are provided
 * as input for mandatory fields. It can also be used whenever the value of a field
 * does not satisfy the required conditions
 */
public class InvalidValueProvidedException extends RuntimeException {
    public InvalidValueProvidedException() { }

    public InvalidValueProvidedException(String message) {
        super(message);
    }

    public InvalidValueProvidedException(String message, Throwable cause) {
        super(message, cause);
    }
}
