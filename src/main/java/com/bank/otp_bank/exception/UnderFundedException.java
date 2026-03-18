package com.bank.otp_bank.exception;

public class UnderFundedException extends RuntimeException {

    public UnderFundedException(String message) {
        super(message);
    }
}
