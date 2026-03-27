package com.bank.otp_bank.db.dto;

import java.util.List;

public record MyCardsResponseDto(
    Long accountId,
    List<CreateCardResponseDto> cards
) {
}
