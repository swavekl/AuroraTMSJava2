package com.auroratms.paymentrefund;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Repository for accessing information on payments and refunds
 */
@RepositoryRestResource
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    List<PaymentRefund> findPaymentRefundsByItemIdAndPaymentRefundFor(long itemId, PaymentRefundFor paymentRefundFor);

}

