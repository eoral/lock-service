package com.eoral.lockservice.controller;

import com.eoral.lockservice.controller.request.AcquireLockRequest;
import com.eoral.lockservice.controller.request.PreAcquireLockRequest;
import com.eoral.lockservice.controller.request.ReleaseLockRequest;
import com.eoral.lockservice.controller.response.PreAcquireLockResponse;
import com.eoral.lockservice.exception.LockAcquiredByAnotherProcessException;
import com.eoral.lockservice.exception.TokenExpiredException;
import com.eoral.lockservice.model.TokenPayload;
import com.eoral.lockservice.service.CommonUtils;
import com.eoral.lockservice.service.JwtUtils;
import com.eoral.lockservice.service.LockPersistence;
import com.eoral.lockservice.service.UniqueIdGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("api")
public class MainController {

    private final LockPersistence lockPersistence;
    private final UniqueIdGenerator uniqueIdGenerator;
    private final JwtUtils jwtUtils;
    private final CommonUtils commonUtils;

    public MainController(
            LockPersistence lockPersistence,
            UniqueIdGenerator uniqueIdGenerator,
            JwtUtils jwtUtils,
            CommonUtils commonUtils) {
        this.lockPersistence = lockPersistence;
        this.uniqueIdGenerator = uniqueIdGenerator;
        this.jwtUtils = jwtUtils;
        this.commonUtils = commonUtils;
    }

    @PostMapping("/pre-acquire-lock")
    public ResponseEntity<PreAcquireLockResponse> preAcquireLock(@RequestBody PreAcquireLockRequest requestBody) {
        commonUtils.validate(requestBody);
        String requestId = uniqueIdGenerator.generate();
        String lockName = requestBody.getLockName();
        TokenPayload tokenPayload = new TokenPayload(requestId, lockName, Instant.now());
        String token = jwtUtils.generateToken(tokenPayload);
        return ResponseEntity.ok().body(new PreAcquireLockResponse(token));
    }

    @PostMapping("/acquire-lock")
    public ResponseEntity<Void> acquireLock(@RequestBody AcquireLockRequest requestBody) {
        commonUtils.validate(requestBody);
        String token = requestBody.getToken();
        TokenPayload tokenPayload = jwtUtils.verifyAndParseToken(token);
        if (commonUtils.isTokenExpired(tokenPayload.getCreationTime(), Instant.now())) {
            throw new TokenExpiredException();
        }
        boolean persisted = lockPersistence.persist(tokenPayload.getLockName(), token, Instant.now());
        if (persisted) {
            return ResponseEntity.ok().build();
        } else {
            throw new LockAcquiredByAnotherProcessException();
        }
    }

    @PostMapping("/release-lock")
    public ResponseEntity<Void> releaseLock(@RequestBody ReleaseLockRequest requestBody) {
        commonUtils.validate(requestBody);
        String token = requestBody.getToken();
        TokenPayload tokenPayload = jwtUtils.verifyAndParseToken(token);
        lockPersistence.delete(tokenPayload.getLockName(), token);
        return ResponseEntity.ok().build();
    }
}