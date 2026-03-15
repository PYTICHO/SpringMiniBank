package com.bank.otp_bank.db.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record UserDto(
    Long id,
    String firstName,
    String lastName,
    String phone,
    String email,
    LocalDateTime createdAt
) {
}
