package com.veth007.hotelmanagement.service.BookingWithPayment.Payment.Implement;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;

import java.util.function.Function;

import com.veth007.hotelmanagement.model.request.Payment.UpdatePaymentRequest;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentGroupBy {
    PAYMENT_STATUS(
        dto -> dto.getPaymentStatus() != null ? dto.getPaymentStatus() : "N/A",
        (filter, value) -> filter.setPaymentStatus(value)
    ),
    PAYMENT_TYPE(
        dto -> dto.getPaymentType() != null ? dto.getPaymentType() : "N/A",
        (filter, value) -> filter.setPaymentType(value)
    ),
    PAYMENT_METHOD(
        dto -> dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "N/A",
        (filter, value) -> filter.setPaymentMethod(value)
    ),
    PAYMENT_TIMING(
        dto -> dto.getPaymentTiming() != null ? dto.getPaymentTiming() : "N/A",
        (filter, value) -> filter.setPaymentTiming(value)
    ),
    CREATED_DATE(dto -> dto.getCreatedDate() != null ?
            dto.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
            (filter, value) -> filter.setCreatedDate(value));


    private final Function<PaymentResponse, String> extractor;
    private final BiConsumer<UpdatePaymentRequest, String> setter;

    public String extract(PaymentResponse payment) {
        return this.extractor.apply(payment);
    }

    public void set(UpdatePaymentRequest request, String value) {
        setter.accept(request, value);
    }
}