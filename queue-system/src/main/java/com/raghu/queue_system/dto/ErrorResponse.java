package com.raghu.queue_system.dto;

import java.util.Map;

public class ErrorResponse {
    private String message;
    private int status;
    private Map<String, String> errors; // 👈 add this

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public ErrorResponse(String message, int status, Map<String, String> errors) {
        this.message = message;
        this.status = status;
        this.errors = errors;
    }

    public String getMessage() { return message; }
    public int getStatus() { return status; }
    public Map<String, String> getErrors() { return errors; }
}
