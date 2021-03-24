package com.auroratms.paymentrefund;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentRefundService {
    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    public PaymentIntent createPaymentIntent(int amount,
                                             int applicationFee,
                                             String currency,
                                             String accountId,
                                             String statementDescriptor,
                                             String customerFullName) throws StripeException {
        Stripe.apiKey = this.stripeApiKey;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("customerFullName", customerFullName);

        if (statementDescriptor != null) {
            metadata.put("paymentFor", statementDescriptor);
            statementDescriptor = (statementDescriptor.length() > 22) ? statementDescriptor.substring(0, 22) : statementDescriptor;
            statementDescriptor = statementDescriptor.replaceAll("[<>\\*\\\"']", " ");
        }

        PaymentIntentCreateParams.TransferData transferData = PaymentIntentCreateParams.TransferData
                .builder()
                .setDestination(accountId)
                .build();

        PaymentIntentCreateParams params2 = PaymentIntentCreateParams
                .builder()
                .putAllMetadata(metadata)
                .setAmount((long)amount)
                .setApplicationFeeAmount((long)applicationFee)
                .setStatementDescriptor(statementDescriptor)
                .setDescription(statementDescriptor)
                .setCurrency(currency)
                .setTransferData(transferData)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD.getValue())
                .build();

        return PaymentIntent.create(params2);
    }
}
