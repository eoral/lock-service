package com.eoral.lockservice.controller.request;

public class PreAcquireLockRequest {

    private String lockName;

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }
}
