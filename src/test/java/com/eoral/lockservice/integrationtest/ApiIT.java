package com.eoral.lockservice.integrationtest;

import com.eoral.lockservice.TestUtils;
import com.eoral.lockservice.controller.response.PreAcquireLockResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ApiIT {

    private static final String DEFAULT_BASE_URL = "http://localhost:8180/api";
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> TOKENS_FOR_LOCKS_TO_BE_CLEANED_UP = Collections.synchronizedList(new ArrayList<>());

    @AfterAll
    static void cleanUp() {
        cleanUpLocks();
    }

    private static void cleanUpLocks() {
        for (String token : TOKENS_FOR_LOCKS_TO_BE_CLEANED_UP) {
            TestUtils.makeReleaseLockRequest(DEFAULT_BASE_URL, DEFAULT_OBJECT_MAPPER, token);
        }
    }

    private static HttpResponse<String> makePreAcquireLockRequest(String lockName) {
        HttpResponse<String> response = TestUtils.makePreAcquireLockRequest(DEFAULT_BASE_URL, DEFAULT_OBJECT_MAPPER, lockName);
        if (response.statusCode() == HttpStatus.OK.value()) {
            PreAcquireLockResponse responseBody = convertJsonToPreAcquireLockResponse(response.body());
            TOKENS_FOR_LOCKS_TO_BE_CLEANED_UP.add(responseBody.getToken());
        }
        return response;
    }

    private static PreAcquireLockResponse convertJsonToPreAcquireLockResponse(String json) {
        return TestUtils.parseJson(DEFAULT_OBJECT_MAPPER, json, PreAcquireLockResponse.class);
    }

    private static HttpResponse<String> makeAcquireLockRequest(String token) {
        return TestUtils.makeAcquireLockRequest(DEFAULT_BASE_URL, DEFAULT_OBJECT_MAPPER, token);
    }

    private static HttpResponse<String> makeReleaseLockRequest(String token) {
        return TestUtils.makeReleaseLockRequest(DEFAULT_BASE_URL, DEFAULT_OBJECT_MAPPER, token);
    }

    private static Callable<HttpResponse<String>> makeAcquireLockRequestAndReturnHttpStatusCode(
            String lockName, CountDownLatch countDownLatch1, CountDownLatch countDownLatch2, CountDownLatch countDownLatch3) {
        return () -> {
            try {
                HttpResponse<String> response1 = makePreAcquireLockRequest(lockName);
                PreAcquireLockResponse responseBody1 = convertJsonToPreAcquireLockResponse(response1.body());
                countDownLatch1.countDown();
                TestUtils.await(countDownLatch2);
                return makeAcquireLockRequest(responseBody1.getToken());
            } finally {
                countDownLatch3.countDown();
            }
        };
    }

    private static void assertOnlyOneResponseIsHttpOk(List<HttpResponse<String>> responses) {
        int httpStatusOkCount = 0;
        int httpStatusConflictCount = 0;
        for (HttpResponse<String> response : responses) {
            if (response.statusCode() == HttpStatus.OK.value()) {
                httpStatusOkCount++;
            } else if (response.statusCode() == HttpStatus.CONFLICT.value()) {
                httpStatusConflictCount++;
            }
        }
        Assertions.assertEquals(1, httpStatusOkCount);
        Assertions.assertEquals(responses.size() - 1, httpStatusConflictCount);
        System.out.println("responseCount: " + responses.size() + ", conflict: " + httpStatusConflictCount);
    }

    @Test
    void preAcquireLockRequestShouldReturnToken() {
        String lockName = TestUtils.generateRandomLockName();
        HttpResponse<String> response = makePreAcquireLockRequest(lockName);
        Assertions.assertEquals(HttpStatus.OK.value(), response.statusCode());
        Assertions.assertTrue(StringUtils.isNotBlank(response.body()));
        PreAcquireLockResponse responseBody = convertJsonToPreAcquireLockResponse(response.body());
        Assertions.assertTrue(StringUtils.isNotBlank(responseBody.getToken()));
    }

    @Test
    void acquireLockRequestShouldReturnHttpOk() {
        String lockName = TestUtils.generateRandomLockName();
        HttpResponse<String> response1 = makePreAcquireLockRequest(lockName);
        PreAcquireLockResponse responseBody1 = convertJsonToPreAcquireLockResponse(response1.body());
        HttpResponse<String> response2 = makeAcquireLockRequest(responseBody1.getToken());
        Assertions.assertEquals(HttpStatus.OK.value(), response2.statusCode());
    }

    @Test
    void acquireLockRequestShouldReturnHttpConflict() {
        String lockName = TestUtils.generateRandomLockName();
        HttpResponse<String> response1 = makePreAcquireLockRequest(lockName);
        PreAcquireLockResponse responseBody1 = convertJsonToPreAcquireLockResponse(response1.body());
        HttpResponse<String> response2 = makeAcquireLockRequest(responseBody1.getToken());
        Assertions.assertEquals(HttpStatus.OK.value(), response2.statusCode());
        // Repeat previous steps from scratch.
        HttpResponse<String> response3 = makePreAcquireLockRequest(lockName);
        PreAcquireLockResponse responseBody3 = convertJsonToPreAcquireLockResponse(response3.body());
        HttpResponse<String> response4 = makeAcquireLockRequest(responseBody3.getToken());
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), response4.statusCode());
    }

    @Test
    void releaseLockRequestShouldReturnHttpOk() {
        String lockName = TestUtils.generateRandomLockName();
        HttpResponse<String> response1 = makePreAcquireLockRequest(lockName);
        PreAcquireLockResponse responseBody1 = convertJsonToPreAcquireLockResponse(response1.body());
        HttpResponse<String> response2 = makeAcquireLockRequest(responseBody1.getToken());
        Assertions.assertEquals(HttpStatus.OK.value(), response2.statusCode());
        HttpResponse<String> response3 = makeReleaseLockRequest(responseBody1.getToken());
        Assertions.assertEquals(HttpStatus.OK.value(), response3.statusCode());
    }

    @Test
    void releaseLockRequestShouldReturnHttpForbidden() {
        String lockName = TestUtils.generateRandomLockName();
        HttpResponse<String> response1 = makePreAcquireLockRequest(lockName);
        PreAcquireLockResponse responseBody1 = convertJsonToPreAcquireLockResponse(response1.body());
        HttpResponse<String> response2 = makeAcquireLockRequest(responseBody1.getToken());
        Assertions.assertEquals(HttpStatus.OK.value(), response2.statusCode());
        // Get another token and try to use it to release the lock.
        HttpResponse<String> response3 = makePreAcquireLockRequest(lockName);
        PreAcquireLockResponse responseBody3 = convertJsonToPreAcquireLockResponse(response3.body());
        HttpResponse<String> response4 = makeReleaseLockRequest(responseBody3.getToken());
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response4.statusCode());
    }

    @Test
    void onlyOneAcquireLockRequestShouldReturnHttpOk() {
        String testName = "concurrent acquire lock request test";
        int iterationCount = 10;
        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        System.out.println(testName + " - numberOfThreads: " + numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 1; i <= iterationCount; i++) {
            System.out.println(testName + " - iteration: " + i);
            String lockName = TestUtils.generateRandomLockName();
            CountDownLatch countDownLatch1 = new CountDownLatch(numberOfThreads);
            CountDownLatch countDownLatch2 = new CountDownLatch(1);
            CountDownLatch countDownLatch3 = new CountDownLatch(numberOfThreads);
            List<Future<HttpResponse<String>>> futures = new ArrayList<>();
            for (int j = 1; j <= numberOfThreads; j++) {
                Callable<HttpResponse<String>> callable = makeAcquireLockRequestAndReturnHttpStatusCode(
                        lockName, countDownLatch1, countDownLatch2, countDownLatch3);
                futures.add(executorService.submit(callable));
            }
            TestUtils.await(countDownLatch1);
            countDownLatch2.countDown();
            TestUtils.await(countDownLatch3);
            List<HttpResponse<String>> responses = futures.stream().map(future -> TestUtils.get(future)).collect(Collectors.toList());
            assertOnlyOneResponseIsHttpOk(responses);
            cleanUpLocks();
        }
        executorService.shutdown();
    }
}
