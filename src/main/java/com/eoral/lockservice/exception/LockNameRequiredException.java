package com.eoral.lockservice.exception;

public class LockNameRequiredException extends BaseException {

    private static final String DEFAULT_MESSAGE = "Lock name is required.";

    public LockNameRequiredException() {
        super(DEFAULT_MESSAGE);
    }
}
