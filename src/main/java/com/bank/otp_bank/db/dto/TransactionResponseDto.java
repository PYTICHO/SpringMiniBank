package com.bank.otp_bank.db.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.otp_bank.db.status.CurrencyStatus;
import com.bank.otp_bank.db.status.TransactionStatus;
import com.bank.otp_bank.db.status.TransactionType;

public record TransactionResponseDto(
    BigDecimal amount,
    CurrencyStatus currency,
    TransactionType type,
    TransactionStatus status,
    String description,
    LocalDateTime created_at,
    Long fromAccountId,
    Long toAccountId
) {

}
