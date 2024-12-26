package com.eoral.lockservice.controller.response;

public class PreAcquireLockResponse {

    private String token;

    public PreAcquireLockResponse() {
    }

    public PreAcquireLockResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
