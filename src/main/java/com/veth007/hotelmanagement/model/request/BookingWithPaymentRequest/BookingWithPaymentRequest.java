package com.veth007.hotelmanagement.model.request.BookingWithPaymentRequest;

import java.io.Serializable;

import com.veth007.hotelmanagement.model.request.Book.BookRequest;
import com.veth007.hotelmanagement.model.request.Payment.PaymentRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingWithPaymentRequest implements Serializable {
    private BookRequest bookRequest;
    private PaymentRequest paymentRequest;
    private String recipientToken; // Add this field to hold the recipient token
}
