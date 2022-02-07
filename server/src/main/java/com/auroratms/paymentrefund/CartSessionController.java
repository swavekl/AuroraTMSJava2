package com.auroratms.paymentrefund;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    @PostMapping("/cartsession/start/{paymentRefundFor}")
    public @ResponseBody
    ResponseEntity<CartSession> startSession(@PathVariable PaymentRefundFor paymentRefundFor) {
        try {
            CartSession cartSession = cartSessionService.startSession(paymentRefundFor);
            URI uri = new URI("/api/cartsession/" + cartSession.getSessionUUID());
            return ResponseEntity.created(uri).body(cartSession);
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
