package com.veth007.hotelmanagement.service.BookingWithPayment.Payment.Implement;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.veth007.hotelmanagement.constant.enums.CurrencyType;

@Component
public class ExchangeRateProvider {

    public BigDecimal convertToUsd(CurrencyType from, BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (from == CurrencyType.USD) {
            return amount;
        }
        return amount.multiply(rateToUsd(from));
    }

    private BigDecimal rateToUsd(CurrencyType from) {
        // replace with a rates table, cache, or external API
        return switch (from) {
            case EUR -> new BigDecimal("1.08");
            case KHR -> new BigDecimal("0.000249");
            default -> throw new IllegalArgumentException("No rate for " + from);
        };
    }
}