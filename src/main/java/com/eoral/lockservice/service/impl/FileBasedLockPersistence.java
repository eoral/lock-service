package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.exception.CannotReleaseLockAcquiredByAnotherProcessException;
import com.eoral.lockservice.model.LockFileContent;
import com.eoral.lockservice.service.CommonUtils;
import com.eoral.lockservice.service.FileUtils;
import com.eoral.lockservice.service.LockPersistence;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

@Service
public class FileBasedLockPersistence implements LockPersistence {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedLockPersistence.class);
    private static final int MAX_TRIAL_COUNT = 3;

    private final CommonUtils commonUtils;
    private final FileUtils fileUtils;
    private final ObjectMapper objectMapper;
    private final Path lockFileDirectoryPath;

    public FileBasedLockPersistence(
            CommonUtils commonUtils,
            FileUtils fileUtils,
            ObjectMapper objectMapper,
            @Value("${lockFileDirectory}") String lockFileDirectory) {
        this.commonUtils = commonUtils;
        this.fileUtils = fileUtils;
        this.objectMapper = objectMapper;
        this.lockFileDirectoryPath = Paths.get(lockFileDirectory);
    }

    @Override
    public boolean persist(String lockName, String token, Instant acquireTime) {
        LockFileContent lockFileContent = new LockFileContent();
        lockFileContent.setLockName(lockName);
        lockFileContent.setAcquireTime(acquireTime.toString());
        lockFileContent.setToken(token);
        Path lockFilePath = getLockFilePath(lockName);
        logger.info("lockFilePath: {}", lockFilePath);
        String json = convertToJson(lockFileContent);
        return fileUtils.createFileIfNotExists(lockFilePath, json, MAX_TRIAL_COUNT);
    }

    @Override
    public void delete(String lockName, String token) {
        Path lockFilePath = getLockFilePath(lockName);
        Optional<String> optional = fileUtils.readFileIfExists(lockFilePath, MAX_TRIAL_COUNT);
        if (optional.isPresent()) {
            String json = optional.get();
            LockFileContent lockFileContent = convertToLockFileContent(json);
            if (lockFileContent.getToken().equals(token)) {
                fileUtils.deleteFileIfExists(lockFilePath, MAX_TRIAL_COUNT);
            } else {
                throw new CannotReleaseLockAcquiredByAnotherProcessException();
            }
        }
    }

    private String convertToJson(LockFileContent lockFileContent) {
        return commonUtils.convertToJson(objectMapper, lockFileContent);
    }

    private LockFileContent convertToLockFileContent(String json) {
        return commonUtils.parseJson(objectMapper, json, LockFileContent.class);
    }

    private Path getLockFilePath(String lockName) {
        String lockFileName = commonUtils.convertToLockFileName(lockName);
        return lockFileDirectoryPath.resolve(lockFileName);
    }
}
