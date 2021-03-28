package com.auroratms.paymentrefund;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
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
     * Translate paymentIntentId to chargeId
     *
     * @param paymentIntentId payment intent id
     * @param accountId       connected account id
     * @return
     * @throws StripeException
     */
    public String getChargeId(String paymentIntentId, String accountId) throws StripeException {
        Stripe.apiKey = this.stripeApiKey;
        String chargeId = null;
        RequestOptions requestParams = RequestOptions
                .builder()
                .setStripeAccount(accountId)
                .build();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId, requestParams);
        ChargeCollection chargesCollection = paymentIntent.getCharges();
        Iterable<Charge> pagingIterable = chargesCollection.autoPagingIterable();
        for (Charge charge : pagingIterable) {
            if (charge.getPaid()) {
                chargeId = charge.getId();
                break;
            }
        }

        return chargeId;
    }

    /**
     * Refunds individual charge associated with the payment intent id
     *  @param refund
     * @param accountId       connected account id
     */
    public Refund refundCharge(PaymentRefund refund, String accountId) throws StripeException {
        Stripe.apiKey = this.stripeApiKey;

        String paymentIntentId = refund.getPaymentIntentId();

        // get charge id for this intent id
        String chargeId = getChargeId(paymentIntentId, accountId);

        RefundCreateParams.Builder builder = RefundCreateParams.builder()
                .setCharge(chargeId)
                .setRefundApplicationFee(true);

        // if a portion of this charge is to be refunded set it
        // in currency requested by user
        // otherwise refund fully
        if (!refund.isRefundFully()) {
            long amount = refund.getPaidAmount();
            builder.setAmount(amount);
        }

        RefundCreateParams params = builder.build();

        RequestOptions requestOptions = RequestOptions
                .builder()
                .setStripeAccount(accountId)
                .build();
        return Refund.create(params, requestOptions);
    }

    /**
     * Records successful payment or refund
     *
     * @param paymentRefund payment or refund to record
     */
    @Transactional
    public PaymentRefund recordPaymentRefund(PaymentRefund paymentRefund) throws StripeException {
        return this.paymentRefundRepository.save(paymentRefund);
    }

    /**
     * Gets all payment refunds for
     *
     * @param itemId
     * @param paymentRefundFor
     * @return
     */
    @Transactional(readOnly = true)
    public List<PaymentRefund> getPaymentRefunds(long itemId, PaymentRefundFor paymentRefundFor) {
        return this.paymentRefundRepository.findPaymentRefundsByItemIdAndPaymentRefundFor(itemId, paymentRefundFor);
    }
}
