package com.bank.otp_bank.db.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.bank.otp_bank.db.status.CardStatus;
import com.bank.otp_bank.db.status.CardType;

public record CreateCardResponseDto(
    String cardNumber,
    String holderName,
    LocalDate expireDate,
    CardType type,
    CardStatus status,
    LocalDateTime createdAt,
    Long accountId
) {

}
