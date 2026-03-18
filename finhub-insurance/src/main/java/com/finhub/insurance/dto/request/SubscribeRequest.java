package com.finhub.insurance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubscribeRequest {

    @NotNull(message = "보험 상품 ID는 필수입니다.")
    private Long productId;
}
