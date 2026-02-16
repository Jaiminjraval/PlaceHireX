package com.placehirex.placementbackend.exception;

import org.springframework.http.HttpStatus;

public class PredictionServiceException extends RuntimeException {
    private final HttpStatus status;

    public PredictionServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public PredictionServiceException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
