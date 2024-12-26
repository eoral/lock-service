package com.eoral.lockservice.service;

import com.eoral.lockservice.model.TokenPayload;

public interface JwtUtils {

    String generateToken(TokenPayload payload);

    TokenPayload verifyAndParseToken(String token);

}
