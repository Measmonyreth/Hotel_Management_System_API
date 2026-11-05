package com.veth007.hotelmanagement.service.BookingWithPayment.Payment.Implement;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaypalService {
    private final APIContext apiContext;

    public Payment createPayment(Double total, String email, String currency, String method,
            String intent, String description, String cancelUrl, String successUrl)
            throws PayPalRESTException {
        Amount amount = new Amount();

        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total));

        // Transaction
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        // Payer
        Payer payer = new Payer();
        payer.setPaymentMethod(method.toUpperCase());
        payer.setPayerInfo(PayerInfo.class.cast(new PayerInfo().setEmail(email)));

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);

    }

      //execute payment

    public Payment execute(String paymentId , String payerId) throws PayPalRESTException {

        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution  paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext,paymentExecution);
    }

}

