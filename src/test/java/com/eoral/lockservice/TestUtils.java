package com.eoral.lockservice;

import com.eoral.lockservice.controller.request.AcquireLockRequest;
import com.eoral.lockservice.controller.request.PreAcquireLockRequest;
import com.eoral.lockservice.controller.request.ReleaseLockRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestUtils {

    public static Path createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteRecursively(Path path) {
        deleteRecursively(path.toFile());
    }

    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteRecursively(f);
            }
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String generateRandomFileNameWithoutExtension() {
        return "test-file-" + UUID.randomUUID();
    }

    public static String generateRandomFileName() {
        return generateRandomFileNameWithoutExtension() + ".txt";
    }

    public static String generateRandomLockName() {
        return "test-lock-" + UUID.randomUUID();
    }

    public static String convertToJson(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T parseJson(ObjectMapper objectMapper, String json, Class<T> objectType) {
        try {
            return objectMapper.readValue(json, objectType);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static HttpResponse<String> makePreAcquireLockRequest(String baseUrl, ObjectMapper objectMapper, String lockName) {
        PreAcquireLockRequest preAcquireLockRequest = new PreAcquireLockRequest();
        preAcquireLockRequest.setLockName(lockName);
        String requestBody = convertToJson(objectMapper, preAcquireLockRequest);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(baseUrl + "/pre-acquire-lock"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse<String> makeAcquireLockRequest(String baseUrl, ObjectMapper objectMapper, String token) {
        AcquireLockRequest acquireLockRequest = new AcquireLockRequest();
        acquireLockRequest.setToken(token);
        String requestBody = convertToJson(objectMapper, acquireLockRequest);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(baseUrl + "/acquire-lock"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse<String> makeReleaseLockRequest(String baseUrl, ObjectMapper objectMapper, String token) {
        ReleaseLockRequest releaseLockRequest = new ReleaseLockRequest();
        releaseLockRequest.setToken(token);
        String requestBody = convertToJson(objectMapper, releaseLockRequest);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(baseUrl + "/release-lock"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void await(CountDownLatch countDownLatch) {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
