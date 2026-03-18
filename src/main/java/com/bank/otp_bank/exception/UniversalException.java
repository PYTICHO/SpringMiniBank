package com.bank.otp_bank.exception;

import org.springframework.http.HttpStatus;

public class UniversalException extends RuntimeException {

    private final HttpStatus status;

    public UniversalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
