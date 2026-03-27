package com.bank.otp_bank.db.dto;

import java.util.List;

public record RecipientSuggestionDto(
    Long userId,
    String firstName,
    String lastName,
    String phone,
    String email,
    List<RecipientCardDto> cards
) {
}
