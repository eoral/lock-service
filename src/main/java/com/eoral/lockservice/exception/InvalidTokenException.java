package com.eoral.lockservice.exception;

public class InvalidTokenException extends BaseException {

    private static final String DEFAULT_MESSAGE = "Token is invalid.";

    public InvalidTokenException() {
        super(DEFAULT_MESSAGE);
    }
}
