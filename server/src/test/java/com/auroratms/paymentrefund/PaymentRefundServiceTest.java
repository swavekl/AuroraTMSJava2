package com.auroratms.paymentrefund;

import com.auroratms.AbstractServiceTest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@Transactional
public class PaymentRefundServiceTest extends AbstractServiceTest {

    @Autowired
    private PaymentRefundService paymentRefundService;

    private String TEST_ACCOUNT_ID = "acct_1IYcqULnh8tr5SoH";

    @Test
    public void testChargeId() {
        try {
            PaymentIntent paymentIntent = paymentRefundService.createPaymentIntent(12500, 100, "usd",
                    TEST_ACCOUNT_ID, "Test Tournament", "Test Player", "bozo@yahoo.com");

            String chargeId = paymentRefundService.getChargeId(paymentIntent.getId(), TEST_ACCOUNT_ID);
            assertNotNull(chargeId, "chargeId is null");
        } catch (StripeException e) {
            System.out.println("e.getStripeError() = " + e.getStripeError());
            System.out.println("e.getCode() = " + e.getCode());
            fail ("error" + e.getMessage());
        }
    }
}
