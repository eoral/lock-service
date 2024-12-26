package com.eoral.lockservice.service;

import com.eoral.lockservice.controller.request.AcquireLockRequest;
import com.eoral.lockservice.controller.request.PreAcquireLockRequest;
import com.eoral.lockservice.controller.request.ReleaseLockRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

public interface CommonUtils {

    void validate(PreAcquireLockRequest requestBody);
    void validate(AcquireLockRequest requestBody);
    void validate(ReleaseLockRequest requestBody);
    boolean isTokenExpired(Instant creationTime, Instant referenceTime);
    String convertToJson(ObjectMapper objectMapper, Object object);
    <T> T parseJson(ObjectMapper objectMapper, String json, Class<T> objectType);
    String convertToLockFileName(String lockName);
}
