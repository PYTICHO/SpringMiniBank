package com.bank.otp_bank.db.dto;

import java.math.BigDecimal;

import com.bank.otp_bank.db.status.CurrencyStatus;

public record AccountSummaryDto(
    Long accountId,
    String accountNumber,
    BigDecimal balance,
    CurrencyStatus currency,
    int cardsCount
) {
}
