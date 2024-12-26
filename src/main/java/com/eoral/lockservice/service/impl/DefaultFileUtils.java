package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.service.FileUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;

@Service
public class DefaultFileUtils implements FileUtils {

    @Override
    public boolean createFileIfNotExists(Path path, String content, int maxTrialCount) {
        if (maxTrialCount < 1 || maxTrialCount > 10) {
            throw new IllegalArgumentException("Max trial count must be between 1 and 10.");
        }
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.SYNC);
            return true;
        } catch (FileAlreadyExistsException e) {
            return false;
        } catch (Exception e) {
            if (maxTrialCount > 1) {
                return createFileIfNotExists(path, content, maxTrialCount - 1);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Optional<String> readFileIfExists(Path path, int maxTrialCount) {
        if (maxTrialCount < 1 || maxTrialCount > 10) {
            throw new IllegalArgumentException("Max trial count must be between 1 and 10.");
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return Optional.of(content);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (Exception e) {
            if (maxTrialCount > 1) {
                return readFileIfExists(path, maxTrialCount - 1);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void deleteFileIfExists(Path path, int maxTrialCount) {
        if (maxTrialCount < 1 || maxTrialCount > 10) {
            throw new IllegalArgumentException("Max trial count must be between 1 and 10.");
        }
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            if (maxTrialCount > 1) {
                deleteFileIfExists(path, maxTrialCount - 1);
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
