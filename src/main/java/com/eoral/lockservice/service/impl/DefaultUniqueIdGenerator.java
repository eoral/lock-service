package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.service.UniqueIdGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultUniqueIdGenerator implements UniqueIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
