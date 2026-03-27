package com.bank.otp_bank.db.dto;

import com.bank.otp_bank.db.status.CardStatus;
import com.bank.otp_bank.db.status.CardType;

public record RecipientCardDto(
    String cardNumber,
    String holderName,
    CardType type,
    CardStatus status
) {
}
