package com.eoral.lockservice.model;

import java.time.Instant;

public class TokenPayload {

    private String requestId;
    private String lockName;
    private Instant creationTime;

    public TokenPayload() {
    }

    public TokenPayload(String requestId, String lockName, Instant creationTime) {
        this.requestId = requestId;
        this.lockName = lockName;
        this.creationTime = creationTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }
}
