package com.eoral.lockservice.exception;

public class TokenExpiredException extends BaseException {

    private static final String DEFAULT_MESSAGE = "Token is expired.";

    public TokenExpiredException() {
        super(DEFAULT_MESSAGE);
    }
}
