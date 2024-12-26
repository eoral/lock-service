package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.controller.request.AcquireLockRequest;
import com.eoral.lockservice.controller.request.PreAcquireLockRequest;
import com.eoral.lockservice.controller.request.ReleaseLockRequest;
import com.eoral.lockservice.exception.LockNameRequiredException;
import com.eoral.lockservice.exception.TokenRequiredException;
import com.eoral.lockservice.service.CommonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Service
public class DefaultCommonUtils implements CommonUtils {

    private final long maxDurationBetweenPreAcquireLockAndAcquireLock; // in milliseconds
    private final Base32 base32;

    public DefaultCommonUtils(
            @Value("${maxDurationBetweenPreAcquireLockAndAcquireLock}") long maxDurationBetweenPreAcquireLockAndAcquireLock) {
        this.maxDurationBetweenPreAcquireLockAndAcquireLock = maxDurationBetweenPreAcquireLockAndAcquireLock;
        this.base32 = new Base32();
    }

    @Override
    public void validate(PreAcquireLockRequest requestBody) {
        if (StringUtils.isBlank(requestBody.getLockName())) {
            throw new LockNameRequiredException();
        }
    }

    @Override
    public void validate(AcquireLockRequest requestBody) {
        if (StringUtils.isBlank(requestBody.getToken())) {
            throw new TokenRequiredException();
        }
    }

    @Override
    public void validate(ReleaseLockRequest requestBody) {
        if (StringUtils.isBlank(requestBody.getToken())) {
            throw new TokenRequiredException();
        }
    }

    /**
     * Ideally, a different token should be used for every acquire-lock request.
     * But, we couldn't find a way that will force users to do that on every acquire-lock request.
     * This check will be OK for now.
     */
    @Override
    public boolean isTokenExpired(Instant creationTime, Instant referenceTime) {
        return (Duration.between(creationTime, referenceTime).toMillis() > maxDurationBetweenPreAcquireLockAndAcquireLock);
    }

    @Override
    public String convertToJson(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> T parseJson(ObjectMapper objectMapper, String json, Class<T> objectType) {
        try {
            return objectMapper.readValue(json, objectType);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String convertToLockFileName(String lockName) {
        // We encode lock name to Base32 to get rid of non-alphanumeric chars.
        // Because, some of those chars may not be allowed in a file name.
        String base32Encoded = base32.encodeToString(lockName.getBytes(StandardCharsets.UTF_8));
        // Base32 encoded strings may end with one or more equal signs: L5PH4PK3LUUCSLBOHIQSPQVDIA======
        // We don't want equal signs. Please check https://superuser.com/a/748264
        String equalSignsRemoved = StringUtils.stripEnd(base32Encoded, "=");
        return equalSignsRemoved + ".json";
    }
}
