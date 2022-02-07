package com.auroratms.paymentrefund;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository for keeping track of cart sessions
 */
public interface CartSessionRepository extends JpaRepository<CartSession, Long> {

    Optional<CartSession> findBySessionUUID(String sessionUUID);

    List<CartSession> findAllByPaymentRefundForAndSessionLastUpdateBeforeOrderBySessionLastUpdate(PaymentRefundFor paymentRefundFor, Date cutoffDateTime);

    // deletes given session
    void deleteBySessionUUID(String sessionUUID);
}
