package com.finhub.payment.repository;

import com.finhub.payment.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.paymentMethod WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    Page<Payment> findByUserIdWithMethod(@Param("userId") Long userId, Pageable pageable);
}
