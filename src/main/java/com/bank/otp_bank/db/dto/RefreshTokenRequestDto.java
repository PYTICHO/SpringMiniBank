package com.bank.otp_bank.db.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
    @NotBlank String refreshToken
) {
}
