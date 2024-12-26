package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.TestConstants;
import com.eoral.lockservice.exception.InvalidTokenException;
import com.eoral.lockservice.model.TokenPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class DefaultJwtUtilsTest {

    @Test
    void generateTokenShouldReturnParsableToken() {
        DefaultJwtUtils defaultJwtUtils = new DefaultJwtUtils(TestConstants.JWT_SECRET_KEY);
        Instant creationTime = Instant.parse("2024-12-24T15:06:45.000Z");
        TokenPayload payload = new TokenPayload();
        payload.setRequestId("my-request-id");
        payload.setLockName("my-lock-name");
        payload.setCreationTime(creationTime);
        String token = defaultJwtUtils.generateToken(payload);
        TokenPayload payloadAfterParse = defaultJwtUtils.verifyAndParseToken(token);
        Assertions.assertEquals(payload.getRequestId(), payloadAfterParse.getRequestId());
        Assertions.assertEquals(payload.getLockName(), payloadAfterParse.getLockName());
        Assertions.assertEquals(payload.getCreationTime(), payloadAfterParse.getCreationTime());
    }

    @Test
    void verifyAndParseTokenShouldThrowInvalidTokenException() {
        DefaultJwtUtils defaultJwtUtils = new DefaultJwtUtils(TestConstants.JWT_SECRET_KEY);
        String invalidToken = "my-invalid-token";
        Assertions.assertThrows(
                InvalidTokenException.class,
                () -> defaultJwtUtils.verifyAndParseToken(invalidToken));
    }
}
