package com.veth007.hotelmanagement.service.BookingWithPayment.Booking_Payment;

import java.util.Map;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.BookingWithPaymentRequest.BookingWithPaymentRequest;
import com.veth007.hotelmanagement.model.response.Book.BookResponse;

public interface BookingWithPayment {
    ApiResponse<BookResponse> createBookingWithPayment(BookingWithPaymentRequest request);
    
    ApiResponse<BookResponse> createBookingWithPaypal(BookingWithPaymentRequest request, String transactionId);

    Map<String, String> initiatePayment(
            String bookId,
            String option,
            Long roomId,
            Long customerId,
            Long employeeId,
            String checkIn,
            String checkOut,
            String bookingType,
                String recipientToken,
            String baseUrl);

    ApiResponse<Object> handlePaymentSuccess(
            Long bookId,
            Long roomId,
            Long customerId,
            Long employeeId,
            String checkIn,
            String checkOut,
            String bookingType,
            String paymentId,
            String payerId,
            String recipientToken
        );

}
