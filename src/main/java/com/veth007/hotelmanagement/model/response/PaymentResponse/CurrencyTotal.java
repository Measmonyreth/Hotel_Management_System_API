package com.veth007.hotelmanagement.model.response.PaymentResponse;

import java.math.BigDecimal;

import com.veth007.hotelmanagement.constant.enums.CurrencyType;

public interface CurrencyTotal {
    CurrencyType getCurrency();
    BigDecimal getTotal();
}
