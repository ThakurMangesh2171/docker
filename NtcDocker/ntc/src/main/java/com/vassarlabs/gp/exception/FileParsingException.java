package com.vassarlabs.gp.exception;

public class FileParsingException extends RuntimeException{
    public FileParsingException() { }

    public FileParsingException(String message) {
        super(message);
    }

    public FileParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
