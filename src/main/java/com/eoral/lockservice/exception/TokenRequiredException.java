package com.eoral.lockservice.exception;

public class TokenRequiredException extends BaseException {

    private static final String DEFAULT_MESSAGE = "Token is required.";

    public TokenRequiredException() {
        super(DEFAULT_MESSAGE);
    }
}
