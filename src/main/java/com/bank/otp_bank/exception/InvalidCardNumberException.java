package com.bank.otp_bank.exception;

public class InvalidCardNumberException extends RuntimeException {

    public InvalidCardNumberException(String message) {
        super(message);
    }
}
