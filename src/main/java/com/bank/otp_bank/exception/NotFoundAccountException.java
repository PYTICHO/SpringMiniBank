package com.bank.otp_bank.exception;


public class NotFoundAccountException extends RuntimeException{
    
    public NotFoundAccountException(String message) {
        super(message);
    }
}
