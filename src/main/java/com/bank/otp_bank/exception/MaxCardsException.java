package com.bank.otp_bank.exception;

public class MaxCardsException extends RuntimeException {
    public MaxCardsException(String message) {
        super(message);
    }
}
