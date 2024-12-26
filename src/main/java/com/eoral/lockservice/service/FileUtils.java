package com.eoral.lockservice.service;

import java.nio.file.Path;
import java.util.Optional;

public interface FileUtils {

    boolean createFileIfNotExists(Path path, String content, int maxTrialCount);

    Optional<String> readFileIfExists(Path path, int maxTrialCount);

    void deleteFileIfExists(Path path, int maxTrialCount);
}
