package com.eoral.lockservice.exception;

public class CannotReleaseLockAcquiredByAnotherProcessException extends BaseException {

    private static final String DEFAULT_MESSAGE = "Cannot release lock acquired by another process.";

    public CannotReleaseLockAcquiredByAnotherProcessException() {
        super(DEFAULT_MESSAGE);
    }
}
