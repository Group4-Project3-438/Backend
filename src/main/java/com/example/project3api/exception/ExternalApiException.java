package com.example.project3api.exception;

public class ExternalApiException extends RuntimeException {
    private final int statusCode;

    public ExternalApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public ExternalApiException(String message, Throwable cause) {
        this(message, 502, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
