package com.bank.otp_bank.db.dto;

import java.math.BigDecimal;

import com.bank.otp_bank.db.status.CurrencyStatus;
import com.bank.otp_bank.db.status.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequestDto(
    @Positive
    @NotNull
    BigDecimal amount,

    @NotNull
    CurrencyStatus currency,

    @NotNull
    TransactionType type,

    String description,

    String to
) {

}
