package com.vassarlabs.gp.exception;

public class FileTemplateMismatchException extends RuntimeException {

    public FileTemplateMismatchException() { }

    public FileTemplateMismatchException(String message) {
        super(message);
    }

    public FileTemplateMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
