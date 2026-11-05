package com.veth007.hotelmanagement.model.response.PaymentResponse;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentListResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalRefund;
    private  long totalPayment;
    private long NumberPaid;
    private long NumberPartialy_paid;
    private long NumberUnpaid;
    private long NumberPartialy_refund;
    private long NumberRefund;
    private List<PaymentResponse> payments;
}
