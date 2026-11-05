package com.veth007.hotelmanagement.model.request.Payment;

import java.math.BigDecimal;

import com.veth007.hotelmanagement.constant.enums.CurrencyType;
import com.veth007.hotelmanagement.constant.enums.PaymentMethod;
import com.veth007.hotelmanagement.constant.enums.PaymentTiming;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Refund amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Currency is required")
    private CurrencyType currency;

    private PaymentTiming paymentTiming;
    private String email;
    private String paymentGetway;
    private String getawayTransactionId;
    private String notes;
}