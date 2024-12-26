package com.eoral.lockservice.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class DefaultUniqueIdGeneratorTest {

    @Test
    void testUniqueness() {
        DefaultUniqueIdGenerator generator = new DefaultUniqueIdGenerator();
        int sampleSize = 1000;
        Set<String> set = new HashSet<>();
        for (int i = 1; i <= sampleSize; i++) {
            set.add(generator.generate());
        }
        Assertions.assertEquals(sampleSize, set.size());
    }
}
