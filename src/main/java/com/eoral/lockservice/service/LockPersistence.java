package com.eoral.lockservice.service;

import java.time.Instant;

public interface LockPersistence {

    boolean persist(String lockName, String token, Instant acquireTime);

    void delete(String lockName, String token);

}
