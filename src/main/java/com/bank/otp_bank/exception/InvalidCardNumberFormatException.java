package com.bank.otp_bank.exception;

public class InvalidCardNumberFormatException extends RuntimeException {

    public InvalidCardNumberFormatException(String message) {
        super(message);
    }
}
