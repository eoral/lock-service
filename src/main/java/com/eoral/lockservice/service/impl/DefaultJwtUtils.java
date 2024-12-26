package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.exception.InvalidTokenException;
import com.eoral.lockservice.model.TokenPayload;
import com.eoral.lockservice.service.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class DefaultJwtUtils implements JwtUtils {

    private static final String CLAIM_REQUEST_ID = "requestId";
    private static final String CLAIM_LOCK_NAME = "lockName";
    private static final String CLAIM_CREATION_TIME = "creationTime";
    private final SecretKey jwtSecretKey_;

    public DefaultJwtUtils(@Value("${jwtSecretKey}") String jwtSecretKey) {
        byte[] bytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        this.jwtSecretKey_ = Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String generateToken(TokenPayload payload) {
        return Jwts.builder()
                .claim(CLAIM_REQUEST_ID, payload.getRequestId())
                .claim(CLAIM_LOCK_NAME, payload.getLockName())
                .claim(CLAIM_CREATION_TIME, payload.getCreationTime().toString())
                .signWith(jwtSecretKey_).compact();
    }

    @Override
    public TokenPayload verifyAndParseToken(String token) {
        Jws<Claims> jws;
        try {
            jws = Jwts.parser().verifyWith(jwtSecretKey_).build().parseSignedClaims(token);
        } catch (Exception e) {
            throw new InvalidTokenException();
        }
        Claims claims = jws.getPayload();
        TokenPayload payload = new TokenPayload();
        payload.setRequestId(claims.get(CLAIM_REQUEST_ID, String.class));
        payload.setLockName(claims.get(CLAIM_LOCK_NAME, String.class));
        payload.setCreationTime(Instant.parse(claims.get(CLAIM_CREATION_TIME, String.class)));
        return payload;
    }
}
