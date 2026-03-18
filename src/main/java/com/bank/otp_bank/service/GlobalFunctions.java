package com.bank.otp_bank.service;

import com.bank.otp_bank.exception.UniversalException;

import org.springframework.http.HttpStatus;

public class GlobalFunctions {

    public static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new UniversalException("Неверный формат номера телефона", HttpStatus.BAD_REQUEST);
        }
    
        String digits = phone.replaceAll("\\D", "");
    
        if (!digits.matches("^[1-9]\\d{10,14}$")) {
            throw new UniversalException("Неверный формат номера телефона", HttpStatus.BAD_REQUEST);
        }
    
        return "+" + digits;
    }
}
