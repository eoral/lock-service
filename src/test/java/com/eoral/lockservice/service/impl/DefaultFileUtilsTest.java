package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.TestConstants;
import com.eoral.lockservice.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

public class DefaultFileUtilsTest {

    private static Path TEMP_DIRECTORY_PATH;

    @BeforeAll
    static void setUp() {
        TEMP_DIRECTORY_PATH = TestUtils.createTempDirectory(TestConstants.TEMP_DIRECTORY_PREFIX);
    }

    @AfterAll
    static void tearDown() {
        TestUtils.deleteRecursively(TEMP_DIRECTORY_PATH);
    }

    @Test
    void createFileIfNotExistsShouldWorkAsExpected() {
        DefaultFileUtils defaultFileUtils = new DefaultFileUtils();
        String fileThatDoesNotExist = TestUtils.generateRandomFileName();
        Path path = TEMP_DIRECTORY_PATH.resolve(fileThatDoesNotExist);
        Assertions.assertFalse(path.toFile().exists());
        String content = "Hello world!";
        Assertions.assertTrue(defaultFileUtils.createFileIfNotExists(path, content, 1));
        Assertions.assertTrue(path.toFile().exists());
        Assertions.assertFalse(defaultFileUtils.createFileIfNotExists(path, content, 1));
    }

    @Test
    void readFileIfExistsShouldWorkAsExpected() {
        DefaultFileUtils defaultFileUtils = new DefaultFileUtils();
        String fileThatDoesNotExist = TestUtils.generateRandomFileName();
        Path path = TEMP_DIRECTORY_PATH.resolve(fileThatDoesNotExist);
        Assertions.assertFalse(path.toFile().exists());
        String content = "Hello world!";
        Optional<String> optional1 = defaultFileUtils.readFileIfExists(path, 1);
        Assertions.assertFalse(optional1.isPresent());
        defaultFileUtils.createFileIfNotExists(path, content, 1);
        Optional<String> optional2 = defaultFileUtils.readFileIfExists(path, 1);
        Assertions.assertTrue(optional2.isPresent());
        Assertions.assertEquals(content, optional2.get());
    }

    @Test
    void deleteFileIfExistsShouldWorkAsExpected() {
        DefaultFileUtils defaultFileUtils = new DefaultFileUtils();
        String fileThatDoesNotExist = TestUtils.generateRandomFileName();
        Path path = TEMP_DIRECTORY_PATH.resolve(fileThatDoesNotExist);
        Assertions.assertFalse(path.toFile().exists());
        String content = "Hello world!";
        defaultFileUtils.deleteFileIfExists(path, 1);
        defaultFileUtils.createFileIfNotExists(path, content, 1);
        Assertions.assertTrue(path.toFile().exists());
        defaultFileUtils.deleteFileIfExists(path, 1);
        Assertions.assertFalse(path.toFile().exists());
    }
}
