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
     * start a session
     *
     * @return
     */
    public CartSession startSession(PaymentRefundFor paymentRefundFor) {
        CartSession cartSession = new CartSession();
        cartSession.setPaymentRefundFor(paymentRefundFor);
        ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();
        UUID uuid = uuidGenerator.generateId(cartSession);
        cartSession.setSessionUUID(uuid.toString());
        cartSession.setSessionLastUpdate(new Date());

        CartSession savedCartSession = cartSessionRepository.saveAndFlush(cartSession);
        return savedCartSession;
    }

    /**
     * updates a session
     *
     * @param sessionUUID
     */
    public void updateSession(String sessionUUID) {
        CartSession cartSession = cartSessionRepository.findBySessionUUID(sessionUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Unable to find cartSession with id " + sessionUUID));
        cartSession.setSessionLastUpdate(new Date());
        cartSessionRepository.saveAndFlush(cartSession);
    }

    /**
     * finishes a session by removing it
     *
     * @param sessionUUID
     */
    public void finishSession(String sessionUUID) {
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

}
