package com.bank.otp_bank.db.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateUserRequestDto(
    @NotBlank 
    String firstName,

    @NotBlank 
    String lastName,

    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{10,14}$")
    String phone,

    @NotBlank 
    @Email 
    String email,

    @NotBlank
    @Size(min = 6, max = 255)
    String password
) {

}
