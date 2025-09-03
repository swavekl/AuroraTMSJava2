package com.auroratms.paymentrefund;

import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Cart session used for determining abandoned cart sessions.  A scheduled job looks for such sessions and removes items that
 * were associated with such sessions.
 */
@Entity
@Table(name = "cart_session", indexes = {
        @Index(name = "idx_sessionid", columnList = "sessionUUID"),
        @Index(name = "idx_paymentandobjectid", columnList = "paymentRefundFor,objectId")
})
public class CartSession {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // GUID identifying the session
    @Column(length = 36)
    @NonNull
    private String sessionUUID;

    // what this was for, so we can find what to act upon if we need to clean-up
    @NonNull
    private PaymentRefundFor paymentRefundFor;

    // creation time and last update time
    @NonNull
    private Date sessionLastUpdate;

    // id of the object for which this is a payment/refund for
    @NonNull
    private Long objectId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionUUID() {
        return sessionUUID;
    }

    public void setSessionUUID(String sessionUUID) {
        this.sessionUUID = sessionUUID;
    }

    public PaymentRefundFor getPaymentRefundFor() {
        return paymentRefundFor;
    }

    public void setPaymentRefundFor(PaymentRefundFor paymentRefundFor) {
        this.paymentRefundFor = paymentRefundFor;
    }

    public Date getSessionLastUpdate() {
        return sessionLastUpdate;
    }

    public void setSessionLastUpdate(Date sessionLastUpdate) {
        this.sessionLastUpdate = sessionLastUpdate;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }
}
