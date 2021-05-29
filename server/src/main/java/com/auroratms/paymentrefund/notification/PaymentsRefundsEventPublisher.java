package com.auroratms.paymentrefund.notification;

import com.auroratms.paymentrefund.notification.event.PaymentEvent;
import com.auroratms.paymentrefund.notification.event.RefundsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publisher for payments and refunds events
 */
@Component
public class PaymentsRefundsEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     *
     * @param event
     */
    public void publishPaymentEvent(PaymentEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public void publishRefundEvents(RefundsEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
