package com.finhub.banking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
        @NotBlank(message = "계좌 이름은 필수입니다.")
        String accountName
) {
}
