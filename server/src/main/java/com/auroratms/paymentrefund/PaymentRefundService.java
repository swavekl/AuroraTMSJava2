package com.auroratms.paymentrefund;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PaymentRefundService {
    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @Autowired
    private PaymentRefundRepository paymentRefundRepository;

    public PaymentIntent createPaymentIntent(int amount,
                                             int applicationFee,
                                             String currency,
                                             String accountId,
                                             String statementDescriptor,
                                             String customerFullName,
                                             String receiptEmail) throws StripeException {
        Stripe.apiKey = this.stripeApiKey;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("customerFullName", customerFullName);

        if (statementDescriptor != null) {
            metadata.put("paymentFor", statementDescriptor);
            statementDescriptor = (statementDescriptor.length() > 22) ? statementDescriptor.substring(0, 22) : statementDescriptor;
            statementDescriptor = statementDescriptor.replaceAll("[<>\\*\\\"']", " ");
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams
                .builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD.getValue())
                .setAmount((long) amount)
                .setCurrency(currency)
                .setApplicationFeeAmount((long) applicationFee)
                .setStatementDescriptor(statementDescriptor)
                .setDescription(statementDescriptor)
                .setReceiptEmail(receiptEmail)
                .putAllMetadata(metadata)
                .build();

        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(accountId)
                .build();
        return PaymentIntent.create(params, requestOptions);
    }

    /**
     *
     * @param paymentIntentId original payment intent used to create a charge
     * @param amount amount to refund
     */
    public Refund refundCharge(String paymentIntentId, long amount) throws StripeException {
        Stripe.apiKey = this.stripeApiKey;

        return Refund.create(RefundCreateParams.builder()
                .setAmount(amount)
                .setPaymentIntent(paymentIntentId)
                .setRefundApplicationFee(true)
                .build());
    }

    /**
     * Records successful payment
     * @param paymentRefund payment or refund to record
     */
    public PaymentRefund recordPayment(PaymentRefund paymentRefund) {
        return this.paymentRefundRepository.save(paymentRefund);
    }

    /**
     * Gets all payment refunds for
     * @param itemId
     * @param paymentRefundFor
     * @return
     */
    public List<PaymentRefund> getPaymentRefunds(long itemId, PaymentRefundFor paymentRefundFor) {
        return this.paymentRefundRepository.findPaymentRefundsByItemIdAndPaymentRefundFor(itemId, paymentRefundFor);
    }
}
