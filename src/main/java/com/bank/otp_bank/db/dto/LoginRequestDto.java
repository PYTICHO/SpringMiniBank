package com.bank.otp_bank.db.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
    @NotBlank 
    @Email 
    String email,

    @NotBlank 
    @Size(min = 6, max = 255) 
    String password
) {
}
