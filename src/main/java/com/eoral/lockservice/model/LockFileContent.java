package com.eoral.lockservice.model;

public class LockFileContent {

    private String lockName;
    private String acquireTime;
    private String token;

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public String getAcquireTime() {
        return acquireTime;
    }

    public void setAcquireTime(String acquireTime) {
        this.acquireTime = acquireTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
