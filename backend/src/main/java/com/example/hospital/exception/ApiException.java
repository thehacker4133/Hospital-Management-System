package com.example.hospital.exception;

import org.springframework.http.HttpStatus;

/** An error with an explicit HTTP status (403, 404, ...). Use IllegalArgumentException for plain 400s. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
