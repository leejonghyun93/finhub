package com.finhub.investment.domain;

import com.finhub.investment.exception.CustomException;
import com.finhub.investment.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal averagePrice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void buy(Long buyQuantity, BigDecimal buyPrice) {
        BigDecimal totalCost = this.averagePrice.multiply(BigDecimal.valueOf(this.quantity))
                .add(buyPrice.multiply(BigDecimal.valueOf(buyQuantity)));
        this.quantity = this.quantity + buyQuantity;
        this.averagePrice = totalCost.divide(BigDecimal.valueOf(this.quantity), 2, RoundingMode.HALF_UP);
    }

    public void sell(Long sellQuantity) {
        if (this.quantity < sellQuantity) {
            throw new CustomException(ErrorCode.INSUFFICIENT_HOLDING);
        }
        this.quantity = this.quantity - sellQuantity;
    }
}
