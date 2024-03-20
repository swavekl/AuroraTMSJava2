package com.auroratms.paymentrefund;

import com.auroratms.error.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@Slf4j
public class CartSessionService {

    @Autowired
    CartSessionRepository cartSessionRepository;

    // Sessions expire in 30 minutes if they are not completed
    private static final int EXPIRED_SESSION_TIMEOUT = 30;

    /**
     * Starts a session with a given start date & time
     *
     * @param paymentRefundFor what is this payment for
     * @param objectId tournament entry id or something else that this is a payment for
     * @param startDate start timestamp of session
     * @return
     */
    public CartSession startSession(PaymentRefundFor paymentRefundFor, long objectId, Date startDate) {
        CartSession cartSession = new CartSession();
        cartSession.setPaymentRefundFor(paymentRefundFor);
        ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();
        UUID uuid = uuidGenerator.generateId(cartSession);
        cartSession.setSessionUUID(uuid.toString());
        cartSession.setSessionLastUpdate(startDate);
        cartSession.setObjectId(objectId);

        CartSession savedCartSession = cartSessionRepository.saveAndFlush(cartSession);
        log.info("Started cart session with id " + savedCartSession.getSessionUUID());
        return savedCartSession;
    }


    /**
     * updates a session
     *
     * @param sessionUUID
     */
    public CartSession updateSession(String sessionUUID) {
        CartSession cartSession = cartSessionRepository.findBySessionUUID(sessionUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find cartSession with id " + sessionUUID));
        cartSession.setSessionLastUpdate(new Date());
        return cartSessionRepository.saveAndFlush(cartSession);
    }

    /**
     * finishes a session by removing it
     *
     * @param sessionUUID
     */
    public void finishSession(String sessionUUID) {
        log.info("Finishing cart session " + sessionUUID);
        cartSessionRepository.deleteBySessionUUID(sessionUUID);
    }

    /**
     * Finds sessions which have not been updated in the max session length time
     *
     * @return list of expired sessions
     */
    public List<CartSession> findExpiredSessions(PaymentRefundFor paymentRefundFor) {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MINUTE, (-1 * EXPIRED_SESSION_TIMEOUT));
        Date cutoffDateTime = calendar.getTime();
        return cartSessionRepository.findAllByPaymentRefundForAndSessionLastUpdateBeforeOrderBySessionLastUpdate(paymentRefundFor, cutoffDateTime);
    }

    public CartSession findSessionFor(PaymentRefundFor paymentRefundFor, long objectId) {
        log.info("Looking for cart session for " + paymentRefundFor + " for objectId " + objectId);
        return cartSessionRepository.findByPaymentRefundForAndObjectId(paymentRefundFor, objectId)
                .orElse(new CartSession());
    }
}
