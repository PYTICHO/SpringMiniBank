package com.bank.otp_bank.db.dto;

import java.time.LocalDateTime;

public record AuthResponseDto(
    Long userId,
    String firstName,
    String lastName,
    String phone,
    String email,
    String accessToken,
    String refreshToken,
    LocalDateTime refreshTokenExpiresAt
) {
}
