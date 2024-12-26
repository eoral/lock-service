package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.TestConstants;
import com.eoral.lockservice.TestUtils;
import com.eoral.lockservice.exception.CannotReleaseLockAcquiredByAnotherProcessException;
import com.eoral.lockservice.model.LockFileContent;
import com.eoral.lockservice.service.CommonUtils;
import com.eoral.lockservice.service.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class FileBasedLockPersistenceTest {

    private static Path TEMP_DIRECTORY_PATH;

    @BeforeAll
    static void setUp() {
        TEMP_DIRECTORY_PATH = TestUtils.createTempDirectory(TestConstants.TEMP_DIRECTORY_PREFIX);
    }

    @AfterAll
    static void tearDown() {
        TestUtils.deleteRecursively(TEMP_DIRECTORY_PATH);
    }

    private CommonUtils commonUtils;
    private FileUtils fileUtils;
    private ObjectMapper objectMapper;
    private String lockFileDirectory;
    private FileBasedLockPersistence fileBasedLockPersistence;

    private void createObjectsUnderTest() {
        commonUtils = new DefaultCommonUtils(TestConstants.MAX_DURATION_BETWEEN_PRE_ACQUIRE_LOCK_AND_ACQUIRE_LOCK);
        fileUtils = new DefaultFileUtils();
        objectMapper = new ObjectMapper();
        lockFileDirectory = TEMP_DIRECTORY_PATH.toString();
        fileBasedLockPersistence = new FileBasedLockPersistence(commonUtils, fileUtils, objectMapper, lockFileDirectory);
    }

    private void assertLockFileIsValid(Path lockFilePath, String expectedLockName, String expectedToken, Instant expectedAcquireTime) {
        Assertions.assertTrue(lockFilePath.toFile().exists());
        String json;
        try {
            json = Files.readString(lockFilePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LockFileContent lockFileContent = TestUtils.parseJson(objectMapper, json, LockFileContent.class);
        Assertions.assertEquals(expectedLockName, lockFileContent.getLockName());
        Assertions.assertEquals(expectedToken, lockFileContent.getToken());
        Assertions.assertEquals(expectedAcquireTime, Instant.parse(lockFileContent.getAcquireTime()));
    }

    @Test
    void persistShouldWorkAsExpected() {
        createObjectsUnderTest();
        String lockName = TestUtils.generateRandomLockName();
        String lockFileName = commonUtils.convertToLockFileName(lockName);
        Path lockFilePath = Paths.get(lockFileDirectory).resolve(lockFileName);
        Assertions.assertFalse(lockFilePath.toFile().exists());
        String token = "my-test-token";
        Instant acquireTime = Instant.parse("2024-12-25T09:45:30.000Z");
        // Should persist because lock file doesn't exist.
        boolean persisted1 = fileBasedLockPersistence.persist(lockName, token, acquireTime);
        Assertions.assertTrue(persisted1);
        assertLockFileIsValid(lockFilePath, lockName, token, acquireTime);
        // Shouldn't persist because lock file exists.
        boolean persisted2 = fileBasedLockPersistence.persist(lockName, token, acquireTime);
        Assertions.assertFalse(persisted2);
    }

    @Test
    void deleteShouldSucceedEvenIfLockFileDoesNotExist() {
        createObjectsUnderTest();
        String lockName = TestUtils.generateRandomLockName();
        String lockFileName = commonUtils.convertToLockFileName(lockName);
        Path lockFilePath = Paths.get(lockFileDirectory).resolve(lockFileName);
        Assertions.assertFalse(lockFilePath.toFile().exists());
        String token = "my-test-token";
        fileBasedLockPersistence.delete(lockName, token);
    }

    @Test
    void deleteShouldSucceedWhenLockFileExistsAndTokenMatches() {
        createObjectsUnderTest();
        String lockName = TestUtils.generateRandomLockName();
        String lockFileName = commonUtils.convertToLockFileName(lockName);
        Path lockFilePath = Paths.get(lockFileDirectory).resolve(lockFileName);
        Assertions.assertFalse(lockFilePath.toFile().exists());
        String token = "my-test-token";
        Instant acquireTime = Instant.parse("2024-12-25T09:45:30.000Z");
        // Should persist because lock file doesn't exist.
        fileBasedLockPersistence.persist(lockName, token, acquireTime);
        Assertions.assertTrue(lockFilePath.toFile().exists());
        // Should delete because lock file exists and token matches.
        fileBasedLockPersistence.delete(lockName, token);
        Assertions.assertFalse(lockFilePath.toFile().exists());
    }

    @Test
    void deleteShouldThrowExceptionWhenLockFileExistsButTokenDoesNotMatch() {
        createObjectsUnderTest();
        String lockName = TestUtils.generateRandomLockName();
        String lockFileName = commonUtils.convertToLockFileName(lockName);
        Path lockFilePath = Paths.get(lockFileDirectory).resolve(lockFileName);
        Assertions.assertFalse(lockFilePath.toFile().exists());
        String token = "my-test-token";
        Instant acquireTime = Instant.parse("2024-12-25T09:45:30.000Z");
        // Should persist because lock file doesn't exist.
        fileBasedLockPersistence.persist(lockName, token, acquireTime);
        Assertions.assertTrue(lockFilePath.toFile().exists());
        // Should throw exception because token doesn't match.
        Assertions.assertThrows(
                CannotReleaseLockAcquiredByAnotherProcessException.class,
                () -> fileBasedLockPersistence.delete(lockName, "another-token"));
    }
}
