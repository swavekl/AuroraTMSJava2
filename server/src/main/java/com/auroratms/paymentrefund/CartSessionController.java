package com.auroratms.paymentrefund;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Date;

/**
 * Controller for initiating new session for cart
 */
@RestController
@RequestMapping("api")
@PreAuthorize("isAuthenticated()")
@Transactional
@Slf4j
public class CartSessionController {

    @Autowired
    private CartSessionService cartSessionService;

    /**
     * Start a new session
     * @param paymentRefundFor
     * @return
     */
    @PostMapping("/cartsession/start/{paymentRefundFor}/{objectId}")
    public @ResponseBody
    ResponseEntity<CartSession> startSession(@PathVariable PaymentRefundFor paymentRefundFor,
                                             @PathVariable long objectId) {
        try {
            CartSession existing = cartSessionService.findSessionFor(paymentRefundFor, objectId);
            if (existing.getSessionUUID() == null) {
                CartSession cartSession = cartSessionService.startSession(paymentRefundFor, objectId, new Date());
                URI uri = new URI("/api/cartsession/" + cartSession.getSessionUUID());
                return ResponseEntity.created(uri).body(cartSession);
            } else {
                log.info("Reused existing session with id " + existing.getSessionUUID() + " which was last updated on " + existing.getSessionLastUpdate());
                CartSession updatedSession = existing;
                if (existing.getSessionLastUpdate().before(new Date())) {
                    updatedSession = cartSessionService.updateSession(existing.getSessionUUID());
                    log.info("Updated existing session with id " + updatedSession.getSessionUUID() + ". The new expiration date is " + updatedSession.getSessionLastUpdate());
                }
                return ResponseEntity.ok(updatedSession);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *
     * @param sessionUUID
     * @return
     */
    @PutMapping("/cartsession/{sessionUUID}")
    public ResponseEntity<Void> update(@PathVariable String sessionUUID) {
        try {
            cartSessionService.updateSession(sessionUUID);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}
