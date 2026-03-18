package com.finhub.insurance.repository;

import com.finhub.insurance.domain.Subscription;
import com.finhub.insurance.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s JOIN FETCH s.product WHERE s.userId = :userId")
    List<Subscription> findByUserIdWithProduct(@Param("userId") Long userId);

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndProductIdAndStatus(Long userId, Long productId, SubscriptionStatus status);
}
