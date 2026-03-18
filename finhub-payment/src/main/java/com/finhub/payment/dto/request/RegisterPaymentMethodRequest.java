package com.finhub.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finhub.payment.domain.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterPaymentMethodRequest {

    @NotNull(message = "결제 수단 유형은 필수입니다.")
    private PaymentMethodType methodType;

    @NotBlank(message = "결제 수단 이름은 필수입니다.")
    private String name;

    private String details;

    @JsonProperty("isDefault")
    private Boolean isDefault;
}
