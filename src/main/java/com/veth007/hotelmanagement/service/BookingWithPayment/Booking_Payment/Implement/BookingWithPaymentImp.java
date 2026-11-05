package com.veth007.hotelmanagement.service.BookingWithPayment.Booking_Payment.Implement;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.constant.enums.BookingType;
import com.veth007.hotelmanagement.model.entity.PaymentHistory;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.BookEntity;
import com.veth007.hotelmanagement.model.entity.BookItemEntity;
import com.veth007.hotelmanagement.model.entity.PaymentEntity;
import com.veth007.hotelmanagement.model.request.Book.BookRequest;
import com.veth007.hotelmanagement.model.request.BookItems.BookItemsRequest;
import com.veth007.hotelmanagement.model.request.BookingWithPaymentRequest.BookingWithPaymentRequest;
import com.veth007.hotelmanagement.model.request.Discount.DiscountRequest;
import com.veth007.hotelmanagement.model.request.Payment.PaymentRequest;
import com.veth007.hotelmanagement.model.response.Book.BookResponse;
import com.veth007.hotelmanagement.repository.BookRepository;
import com.veth007.hotelmanagement.repository.PaymentHistoryRepository;
import com.veth007.hotelmanagement.repository.PaymentRepository;
import com.veth007.hotelmanagement.service.BookingWithPayment.Book.BookServiceImp;
import com.veth007.hotelmanagement.service.BookingWithPayment.Booking_Payment.BookingWithPayment;
import com.veth007.hotelmanagement.service.BookingWithPayment.Payment.Implement.PaymentServiceImp;
import com.veth007.hotelmanagement.service.BookingWithPayment.Payment.Implement.PaypalService;
import com.veth007.hotelmanagement.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingWithPaymentImp implements BookingWithPayment {
    private static final Logger log = LoggerFactory.getLogger(BookingWithPaymentImp.class);

    private final BookServiceImp bookService;
    private final PaymentServiceImp paymentService;
    private final BookRepository bookRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaypalService paypalService;
    private final HashIdUtil hashIdUtil;
    // needed so internal calls go through the Spring proxy and keep @Transactional
    private final ObjectProvider<BookingWithPayment> self;

    private static final String SUCCESS_PATH = "/api/payment/success";
    private static final String CANCEL_PATH = "/api/payment/cancel";

    // ─────────────────────────────────────────
    // ONE transaction: book + payment together
    // if payment fails → everything rolls back
    // ─────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<BookResponse> createBookingWithPayment(BookingWithPaymentRequest request) {
        BookItemEntity newItem = bookService.createBooking(request.getBookRequest());

        PaymentEntity paymentEntity = paymentService.processPayment(
                PaymentRequest.builder()
                        .bookId(newItem.getBook().getId())
                        .paidAmount(request.getPaymentRequest().getPaidAmount())
                        .description(request.getPaymentRequest().getDescription())
                        .paymentMethod(request.getPaymentRequest().getPaymentMethod())
                        .paymentTiming(request.getPaymentRequest().getPaymentTiming())
                        .currency(request.getPaymentRequest().getCurrency())
                        .paymentDate(LocalDateTime.now())
                        .note(request.getPaymentRequest().getNote())
                        .build());
        BookEntity bookEntity = bookRepository.findById(newItem.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload booking after payment"));

        Long customerId = request.getBookRequest().getCustomerId();
        Long bookingId = newItem.getBook().getId();
        String roomNumber = newItem.getRoom().getRoomNumber();
        var paymentStatus = paymentEntity.getPaymentStatus();
        int amountPaid = request.getPaymentRequest().getPaidAmount().intValue();
        String recipientToken = request.getRecipientToken();

        Runnable sendBookingNotification = () -> {
            try {
                notificationService.bookingSuccess(
                        customerId, bookingId, roomNumber, paymentStatus,
                        amountPaid, 0, recipientToken);
            } catch (Exception ex) {
                log.warn("Booking saved but notification failed for bookId={}", bookingId, ex);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendBookingNotification.run();
                }
            });
        } else {
            sendBookingNotification.run();
        }

        return ApiResponse.success(BookResponse.fromEntity(bookEntity, newItem));
    }

    // ─────────────────────────────────────────
    // ONE transaction: book + PayPal payment.
    // Booking is saved only when the PayPal
    // payment succeeded; any failure rolls
    // back the whole booking.
    // ─────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<BookResponse> createBookingWithPaypal(BookingWithPaymentRequest request,
            String transactionId) {
        BookItemEntity newItem = bookService.createBooking(request.getBookRequest());

        PaymentEntity paymentEntity = paymentService.processPayment(
                PaymentRequest.builder()
                        .bookId(newItem.getBook().getId())
                        .paidAmount(request.getPaymentRequest().getPaidAmount())
                        .email(request.getPaymentRequest().getEmail())
                        .description(request.getPaymentRequest().getDescription())
                        .paymentMethod(request.getPaymentRequest().getPaymentMethod())
                        .paymentTiming(request.getPaymentRequest().getPaymentTiming())
                        .currency(request.getPaymentRequest().getCurrency())
                        .paymentDate(LocalDateTime.now())
                        .note(request.getPaymentRequest().getNote())
                        .build());
        paymentEntity.setPaymentGetway("PAYPAL");
        paymentEntity.setGetwayTransactionId(transactionId);
        paymentRepository.save(paymentEntity);

        List<PaymentHistory> histories = paymentHistoryRepository.findAllByPaymentId(paymentEntity.getId());
        for (PaymentHistory history : histories) {
            history.setPaymentGetway("PAYPAL");
            history.setGetwayTransactionId(transactionId);
            history.setEmail(request.getPaymentRequest().getEmail());
        }
        paymentHistoryRepository.saveAll(histories);

        BookEntity bookEntity = bookRepository.findById(newItem.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Failed to reload booking after payment"));

        Long customerId = request.getBookRequest().getCustomerId();
        Long bookingId = newItem.getBook().getId();
        String roomNumber = newItem.getRoom().getRoomNumber();
        var paymentStatus = paymentEntity.getPaymentStatus();
        int amountPaid = request.getPaymentRequest().getPaidAmount().intValue();
        String recipientToken = request.getRecipientToken();

        Runnable sendBookingNotification = () -> {
            try {
                notificationService.bookingSuccess(
                        customerId, bookingId, roomNumber, paymentStatus,
                        amountPaid, 0, recipientToken);
            } catch (Exception ex) {
                log.warn("Booking saved but notification failed for bookId={}", bookingId, ex);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendBookingNotification.run();
                }
            });
        } else {
            sendBookingNotification.run();
        }

        return ApiResponse.success(BookResponse.fromEntity(bookEntity, newItem));
    }

    // ─────────────────────────────────────────
    // Initiate PayPal payment
    // Extract payment logic from controller
    // ─────────────────────────────────────────
    @Override
    public Map<String, String> initiatePayment(
            String bookId,
            String option,
            Long roomId,
            Long customerId,
            Long employeeId,
            String checkIn,
            String checkOut,
            String bookingType,
            String recipientToken,
            String baseUrl) {

        BigDecimal amount;
        String successUrl;
        String description;

        if (bookId != null && !bookId.isBlank()) {
            Long id = resolveBookId(bookId);
            amount = paymentService.calculatePaypalAmount(id, option);
            successUrl = appendRecipientToken(baseUrl + SUCCESS_PATH + "?bookId=" + id, recipientToken);
            description = ("DEPOSIT_30".equalsIgnoreCase(option) ? "30% deposit" : "Full payment")
                    + " for booking " + id;
        } else {
            if (roomId == null || customerId == null
                    || checkIn == null || checkOut == null) {
                return Map.of("error", "Missing booking details (roomId, customerId, checkIn, checkOut)");
            }

            BigDecimal total = bookService.quotePrice(roomId, customerId, checkIn, checkOut);
            amount = "DEPOSIT_30".equalsIgnoreCase(option)
                    ? total.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP)
                    : total;
            successUrl = appendRecipientToken(
                    baseUrl + SUCCESS_PATH + "?roomId=" + roomId + "&customerId=" + customerId
                            + (employeeId != null ? "&employeeId=" + employeeId : "")
                            + "&checkIn=" + checkIn
                            + "&checkOut=" + checkOut + "&bookingType=" + bookingType,
                    recipientToken);
            description = ("DEPOSIT_30".equalsIgnoreCase(option) ? "30% deposit" : "Full payment")
                    + " for room " + roomId + " (" + checkIn + " to " + checkOut + ")";
        }

        try {
            Payment payment = paypalService.createPayment(
                    amount.doubleValue(),
                    null,
                    "USD",
                    "paypal",
                    "sale",
                    description,
                    baseUrl + CANCEL_PATH,
                    successUrl);
            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return Map.of(
                            "redirectUrl", links.getHref(),
                            "amount", amount.toPlainString());
                }
            }
        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }

        return Map.of("error", "Error processing the payment");
    }

    private Long resolveBookId(String bookId) {
        if (bookId.matches("\\d+")) {
            return Long.valueOf(bookId);
        }
        return hashIdUtil.decode(bookId);
    }

    private String appendRecipientToken(String successUrl, String recipientToken) {
        if (recipientToken == null || recipientToken.isBlank()) {
            return successUrl;
        }
        return successUrl + "&recipientToken="
                + URLEncoder.encode(recipientToken.trim(), StandardCharsets.UTF_8);
    }

    // ─────────────────────────────────────────
    // Handle PayPal payment success callback
    // Executes payment and creates booking if needed
    // ─────────────────────────────────────────
    @Override
    public ApiResponse<Object> handlePaymentSuccess(
            Long bookId,
            Long roomId,
            Long customerId,
            Long employeeId,
            String checkIn,
            String checkOut,
            String bookingType,
            String paymentId,
            String payerId,
            String recipientToken) {

        try {
            // external PayPal call stays outside the DB transaction
            Payment payment = paypalService.execute(paymentId, payerId);
            if (!payment.getState().equals("approved")) {
                return ApiResponse.error("Payment was not approved by PayPal", 400);
            }

            BigDecimal amount = new BigDecimal(
                    payment.getTransactions().get(0).getAmount().getTotal());

            String payerEmail = payment.getPayer() != null && payment.getPayer().getPayerInfo() != null
                    ? payment.getPayer().getPayerInfo().getEmail()
                    : null;

            // existing booking → just record the payment
            if (bookId != null) {
                // unwrap so clients get PaymentResponse in data, not a nested ApiResponse
                ApiResponse<?> paid = paymentService.payment_booking_with_paypal(
                        bookId, amount, paymentId, payerEmail);
                return paid.isSuccess()
                        ? ApiResponse.success(paid.getData())
                        : ApiResponse.error(paid.getMessage(), paid.getStatus());
            }

            // pay-first flow → book + payment in ONE transaction;
            // if anything fails the booking rolls back completely
            BookingWithPaymentRequest request = BookingWithPaymentRequest.builder()
                    .bookRequest(BookRequest.builder()
                            .customerId(customerId)
                            .employeeId(employeeId)
                            .checkInDate(checkIn)
                            .checkOutDate(checkOut)
                            .bookingType(bookingType != null ? bookingType : BookingType.ONLINE.name())
                            .bookItemsRequest(List.of(BookItemsRequest.builder()
                                    .roomId(roomId)
                                    // .status("PENDING")
                                    .build()))
                            .discountRequest(DiscountRequest.builder()
                                    .description("Booking discount")
                                    .type("GENERAL")
                                    .valid_from(checkIn)
                                    .valid_until(checkOut)
                                    .build())
                            .build())
                    .paymentRequest(PaymentRequest.builder()
                            .paidAmount(amount)
                            .email(payerEmail)
                            .currency("USD")
                            .paymentMethod("PAYPAL")
                            .paymentTiming("ONLINE_APP")
                            .description("PayPal booking payment for room " + roomId)
                            .note("Paid via PayPal")
                            .build())
                    .recipientToken(recipientToken)
                    .build();

            // MUST go through Spring proxy so @Transactional on createBookingWithPaypal
            // applies.
            // Direct this.createBookingWithPaypal(...) skips the proxy → MANDATORY TX
            // error.
            // unwrap so clients get BookResponse in data, not a nested ApiResponse
            ApiResponse<BookResponse> booked = self.getObject().createBookingWithPaypal(request, paymentId);
            return booked.isSuccess()
                    ? ApiResponse.success(booked.getData())
                    : ApiResponse.error(booked.getMessage(), booked.getStatus());
        } catch (PayPalRESTException e) {
            throw new RuntimeException(e);
        }
    }
}