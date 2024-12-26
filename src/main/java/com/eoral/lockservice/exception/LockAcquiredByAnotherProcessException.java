package com.eoral.lockservice.exception;

public class LockAcquiredByAnotherProcessException extends BaseException {

    private static final String DEFAULT_MESSAGE = "Lock acquired by another process.";

    public LockAcquiredByAnotherProcessException() {
        super(DEFAULT_MESSAGE);
    }
}
