package com.finhub.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PayRequest {

    @NotNull(message = "결제 수단 ID는 필수입니다.")
    private Long paymentMethodId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @DecimalMin(value = "1", message = "결제 금액은 1원 이상이어야 합니다.")
    private BigDecimal amount;

    private String description;
}
