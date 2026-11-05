package com.veth007.hotelmanagement.service.BookingWithPayment.Payment.Implement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.veth007.hotelmanagement.constant.enums.CurrencyType;
import com.veth007.hotelmanagement.constant.enums.PaymentMethod;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.constant.enums.PaymentTiming;
import com.veth007.hotelmanagement.constant.enums.PaymentType;
import com.veth007.hotelmanagement.constant.enums.RefundStatus;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.BusinessException;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.BookEntity;
import com.veth007.hotelmanagement.model.entity.CancelRequestEntity;
import com.veth007.hotelmanagement.model.entity.PaymentEntity;
import com.veth007.hotelmanagement.model.entity.PaymentHistory;
import com.veth007.hotelmanagement.model.request.Cancel.CancelRequest;
import com.veth007.hotelmanagement.model.request.Payment.PaymentRequest;
import com.veth007.hotelmanagement.model.request.Payment.RefundRequest;
import com.veth007.hotelmanagement.model.response.PaymentHistoryResponse;
import com.veth007.hotelmanagement.model.request.Payment.UpdatePaymentRequest;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentListResponse;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentResponse;
import com.veth007.hotelmanagement.repository.BookRepository;
import com.veth007.hotelmanagement.repository.CancelRepository;
import com.veth007.hotelmanagement.repository.PaymentHistoryRepository;
import com.veth007.hotelmanagement.repository.PaymentRepository;
import com.veth007.hotelmanagement.service.BookingWithPayment.Payment.PaymentService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService {
    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Phnom_Penh");
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final BookRepository bookRepository;
    

    private static final List<PaymentStatus> NET_REVENUE_STATUSES = List.of(
            PaymentStatus.PARTIALLY_PAID,
            PaymentStatus.PAID,
            PaymentStatus.PARTIALLY_REFUNDED,
            PaymentStatus.REFUNDED);
    private static final List<PaymentStatus> REFUND_STATUSES = List.of(
            PaymentStatus.PARTIALLY_REFUNDED,
            PaymentStatus.REFUNDED);
    private final ExchangeRateProvider rateProvider;
    private final CancelRepository cancelRepo;

    public BigDecimal totalNetInUsd() {
        return paymentRepository.sumNetByCurrency(NET_REVENUE_STATUSES).stream()
                .map(row -> rateProvider.convertToUsd(row.getCurrency(), row.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public ApiResponse<PaymentHistoryResponse> refund(Long paymentId, RefundRequest request) {
        PaymentEntity payment = paymentRepository.findActiveByIdWithBookItems(paymentId);
        if (payment == null) {
            throw new NotFoundException("Payment not found: " + paymentId);
        }

        CancelRequestEntity requestEntity = cancelRepo.findByBookId(payment.getBook().getId());
        if (requestEntity == null) {
            throw new BusinessException(
                    "No cancellation request found for booking ID: " + payment.getBook().getId());
        }

        if(requestEntity.getStatus() != RefundStatus.PROCESSING){
            throw new BusinessException("Refund is not approved.");
        }

        BookEntity bookEntity = payment.getBook();
        if(bookEntity.getPaymentStatus() != PaymentStatus.PAID){
            throw new BusinessException("Booking is not paid yet.");
        }

        BigDecimal refunded = paymentHistoryRepository.findAllByPaymentId(paymentId).stream()
                .filter(history -> history.getDeletedDate() == null
                        && history.getPaymentType() == PaymentType.REFUND)
                .map(PaymentHistory::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal available = payment.getPaidAmount().subtract(refunded);
        BigDecimal refundRate = refundRateUntilCheckIn(payment);
        BigDecimal policyRefund = payment.getPaidAmount()
                .multiply(refundRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal policyAvailable = policyRefund.subtract(refunded).max(BigDecimal.ZERO);
        BigDecimal refundAmount = request != null && request.getAmount() != null
                ? request.getAmount()
                : policyRefund;

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("No refund is available for this booking");
        }
        if (refundAmount.compareTo(policyAvailable) > 0) {
            throw new BusinessException("Refund amount exceeds the remaining policy refund amount: "
                    + policyAvailable);
        }
        if (refundAmount.compareTo(available) > 0) {
            throw new BusinessException("Refund amount exceeds refundable amount: " + available);
        }

        BigDecimal totalRefunded = refunded.add(refundAmount);
        payment.setRefundedAmount(totalRefunded);
        payment.setPaymentStatus(totalRefunded.compareTo(policyRefund) >= 0
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED);

        paymentRepository.save(payment);

        PaymentMethod refundMethod = request != null && request.getPaymentMethod() != null
                ? request.getPaymentMethod()
                : payment.getPaymentMethod();
        CurrencyType refundCurrency = request != null && request.getCurrency() != null
                ? request.getCurrency()
                : payment.getCurrency();
        PaymentTiming refundTiming = request != null ? request.getPaymentTiming() : payment.getPaymentTiming();
        String refundEmail = request != null ? request.getEmail() : payment.getEmail();
        String refundGateway = request != null ? request.getPaymentGetway() : payment.getPaymentGetway();
        String refundGatewayTransactionId = request != null
                ? request.getGetawayTransactionId()
                : payment.getGetwayTransactionId();

        if (refundMethod == null) {
            throw new BusinessException("Payment method is required for refund history");
        }
        if (refundCurrency == null) {
            throw new BusinessException("Currency is required for refund history");
        }

        BigDecimal totolpanaltyfee = payment.getPaidAmount().subtract(totalRefunded);

        requestEntity.setCalculatedRefundAmount(requestEntity.getCalculatedRefundAmount().add(totalRefunded));
        requestEntity.setCalculatedPenaltyFee(requestEntity.getCalculatedPenaltyFee().add(totolpanaltyfee));
        requestEntity.setStatus(RefundStatus.APPROVED);
        cancelRepo.save(requestEntity);


        PaymentHistory history = new PaymentHistory();
        history.setPayment(payment);
        history.setPaymentType(PaymentType.REFUND);
        history.setPaymentMethod(refundMethod);
        history.setEmail(refundEmail);
        history.setPaymentTiming(refundTiming);
        history.setCurrency(refundCurrency);
        history.setAmount(refundAmount);
        history.setPaymentGetway(refundGateway);
        history.setGetwayTransactionId(refundGatewayTransactionId);
        history.setPaymentDate(LocalDateTime.now());
        history.setNotes(request != null && request.getNotes() != null
                ? request.getNotes()
                : "Refund policy rate: " + refundRate.multiply(BigDecimal.valueOf(100)) + "%");

        PaymentHistory saved = paymentHistoryRepository.save(history);
        return ApiResponse.success(PaymentHistoryResponse.fromEntity(saved));
    }

    private BigDecimal refundRateUntilCheckIn(PaymentEntity payment) {
        LocalDateTime now = LocalDateTime.now(HOTEL_ZONE);
        BookEntity book = payment.getBook();
        if (book == null || book.getCheckInDate() == null) {
            throw new BusinessException("Booking check-in date not found");
        }

        return refundRateUntilCheckIn(book.getCheckInDate(), now);
    }

    private BigDecimal refundRateUntilCheckIn(LocalDateTime checkIn, LocalDateTime now) {
        long minutesUntilCheckIn = Duration.between(now, checkIn).toMinutes();
        if (minutesUntilCheckIn <= 0) {
            return BigDecimal.ZERO;
        }
        if (minutesUntilCheckIn >= 48 * 60) {
            return new BigDecimal("0.70");
        }
        if (minutesUntilCheckIn >= 24 * 60) {
            return new BigDecimal("0.50");
        }
        if (minutesUntilCheckIn >= 12 * 60) {
            return new BigDecimal("0.20");
        }
        return BigDecimal.ZERO;
    }

    private PaymentListResponse paymentListResponse(List<PaymentEntity> paymentEntities) {
        BigDecimal totalRefund = paymentRepository.sumRefundedAmount(REFUND_STATUSES);
        long numberPaid = paymentRepository.countActiveByPaymentStatus(PaymentStatus.PAID);
        long numberPartialy_paid = paymentRepository.countActiveByPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        long NumberUnpaid = paymentRepository.countActiveByPaymentStatus(PaymentStatus.UNPAID);
        long totalPayment = paymentRepository.count();
        long NumberPartialy_refund = paymentRepository.countActiveByPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        long NumberRefund = paymentRepository.countActiveByPaymentStatus(PaymentStatus.REFUNDED);

        return PaymentListResponse.builder()
                .totalPayment(totalPayment)
                .totalRevenue(totalNetInUsd())
                .totalRefund(totalRefund)
                .NumberPaid(numberPaid)
                .NumberPartialy_paid(numberPartialy_paid)
                .NumberUnpaid(NumberUnpaid)
                .NumberPartialy_refund(NumberPartialy_refund)
                .NumberRefund(NumberRefund)
                .payments(paymentEntities.stream()
                        .map(PaymentResponse::fromEntity)
                        .toList())
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public PaymentEntity processPayment(PaymentRequest request) {
        BookEntity book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (book.getPaymentStatus() == PaymentStatus.PAID)
            throw new AlreadyExistException("Booking is already fully paid.");
        if (book.getPaymentStatus() == PaymentStatus.REFUNDED)
            throw new AlreadyExistException("Booking has been refunded.");

        BigDecimal payAmount = request.getPaidAmount();
        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BusinessException("Payment amount must be greater than zero.");

        String currency = request.getCurrency() != null ? request.getCurrency().trim().toUpperCase()
                : CurrencyType.USD.name();

        BigDecimal remaining = book.getRemainingAmount() != null ? book.getRemainingAmount() : book.getTotalAmount();
        if (payAmount.compareTo(remaining) > 0)
            throw new BusinessException("Payment amount " + payAmount + " exceeds remaining balance " + remaining);

        BigDecimal currentRemaining = book.getRemainingAmount() != null ? book.getRemainingAmount()
                : book.getTotalAmount();

        // Always create a new PaymentEntity for each processed payment (do not modify
        // existing payments)
        PaymentEntity p = new PaymentEntity();
        p.setBook(book);
        p.setEmail(request.getEmail());
        p.setCode(book.getCode() + "-PAYMENT");
        p.setDescription(request.getDescription());
        p.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        p.setPaymentTiming(PaymentTiming.valueOf(request.getPaymentTiming()));
        p.setPaymentDate(LocalDateTime.now());
        p.setPaidAmount(payAmount);
        p.setNote(request.getNote());
        p.setCurrency(CurrencyType.valueOf(currency));

        BigDecimal existingPaid = book.getPaidAmount() != null ? book.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal depositAfterPayment = existingPaid.add(payAmount);
        boolean isFullyPaid = depositAfterPayment.compareTo(book.getTotalAmount()) >= 0;

        p.setPaymentStatus(isFullyPaid ? PaymentStatus.PAID : PaymentStatus.PARTIALLY_PAID);
        p.setPaymentType(isFullyPaid ? PaymentType.FULL_PAYMENT : PaymentType.DEPOSIT_PAID);

        if (isFullyPaid) {
            // Once the booking balance is settled, mark its earlier payment records
            // as paid too. The current payment `p` is saved below with PAID status.
            List<PaymentEntity> previousPayments = paymentRepository
                    .findAllByBookIdAndDeletedDateIsNull(book.getId());
            previousPayments.forEach(previousPayment -> previousPayment.setPaymentStatus(PaymentStatus.PAID));
            paymentRepository.saveAll(previousPayments);
        }

        paymentRepository.save(p);

        PaymentHistory history = new PaymentHistory();
        history.setCurrency(CurrencyType.valueOf(currency));
        history.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        history.setPaymentTiming(PaymentTiming.valueOf(request.getPaymentTiming()));
        history.setPayment(p);
        history.setAmount(payAmount);
        history.setPaymentType(isFullyPaid ? PaymentType.FULL_PAYMENT : PaymentType.DEPOSIT_PAID);
        history.setPaymentDate(LocalDateTime.now());
        history.setNotes(isFullyPaid ? "Full payment" : "Deposit payment");
        paymentHistoryRepository.save(history);

        BigDecimal remainingAmount_book = book.getTotalAmount() != null
                ? book.getTotalAmount().subtract(depositAfterPayment).max(BigDecimal.ZERO)
                : BigDecimal.ZERO;

        book.setPaidAmount(depositAfterPayment);
        book.setRemainingAmount(remainingAmount_book);
        book.setPaymentStatus(isFullyPaid ? PaymentStatus.PAID : PaymentStatus.PARTIALLY_PAID);


        bookRepository.save(book);
        return p;

}
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PaymentResponse> payment_booking(PaymentRequest request) {
        return ApiResponse.success(PaymentResponse.fromEntity(this.processPayment(request), true));
    }

    @Override
    public BigDecimal calculatePaypalAmount(Long bookId, String option) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (book.getPaymentStatus() == PaymentStatus.PAID)
            throw new AlreadyExistException("Booking is already fully paid.");
        if (book.getPaymentStatus() == PaymentStatus.REFUNDED)
            throw new AlreadyExistException("Booking has been refunded.");

        BigDecimal remaining = book.getRemainingAmount() != null ? book.getRemainingAmount() : book.getTotalAmount();

        if ("DEPOSIT_30".equalsIgnoreCase(option)) {
            BigDecimal deposit = book.getTotalAmount()
                    .multiply(new BigDecimal("0.30"))
                    .setScale(2, RoundingMode.HALF_UP);
            return deposit.min(remaining);
        }
        return remaining.setScale(2, RoundingMode.HALF_UP);
    }

    // payments that are not complete yet: the remaining balance
    // can be paid later on the PayPal payment page
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PaymentListResponse> getIncompletePayments() {
        List<PaymentStatus> statuses = List.of(PaymentStatus.UNPAID, PaymentStatus.PARTIALLY_PAID);
        return ApiResponse
                .success(paymentListResponse(paymentRepository.findByPaymentStatusInAndDeletedDateIsNull(statuses)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PaymentResponse> payment_booking_with_paypal(Long bookId, BigDecimal amount,
            String transactionId, String email) {
        PaymentRequest request = PaymentRequest.builder()
                .bookId(bookId)
                .paidAmount(amount)
                .email(email)
                .currency(CurrencyType.USD.name())
                .paymentMethod(PaymentMethod.PAYPAL.name())
                .paymentTiming(PaymentTiming.ONLINE_APP.name())
                .description("PayPal payment for booking " + bookId)
                .note("Paid via PayPal")
                .build();

        PaymentEntity payment = this.processPayment(request);
        payment.setPaymentGetway("PAYPAL");
        payment.setGetwayTransactionId(transactionId);
        paymentRepository.save(payment);

        return ApiResponse.success(PaymentResponse.fromEntity(payment, true));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PaymentListResponse> get_all(int page, int size, boolean isPage) {
        try {
            List<PaymentEntity> paymentEntities;
            if (isPage) {
                int pageIndex = Math.max(page, 0);
                Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdDate"));
                paymentEntities = paymentRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdDate"));
                paymentEntities = paymentRepository.findAll(pageable).getContent();
            }
            return ApiResponse.success(paymentListResponse(paymentEntities));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all payments: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PaymentResponse> get_by_id(Long id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
        try {

            return ApiResponse.success(PaymentResponse.fromEntity(payment, true));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get payment: " + e.getMessage());
        }
    }

    // @Transactional(readOnly = true)
    // public PaymentEntity findOne(Long id) {
    // return paymentRepository.findById(id)
    // .filter(p -> p.getDeletedDate() == null)
    // .orElseThrow(() -> new NotFoundException("Payment not found with id: " +
    // id));
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> delete(Long id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));

        if (payment.getDeletedDate() != null)
            throw new BusinessException("Payment already deleted");
        if (payment.getPaymentStatus() == PaymentStatus.PAID)
            throw new BusinessException("Cannot delete a fully paid payment");
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED)
            throw new BusinessException("Cannot delete a refunded payment");
        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            payment.setDeletedDate(LocalDateTime.now());
            payment.setDeletedBy(username);
            paymentRepository.save(payment);

            return ApiResponse.success("Payment deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete payment: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiResponse<String> restore(Long id) {
        if (!paymentRepository.existsById(id)) {

            throw new NotFoundException("Payment not found");

        }

        PaymentEntity payment = paymentRepository.findByIdAndDeletedDateIsNotNull(id)
                .orElseThrow(() -> new BusinessException("Payment not found or not deleted"));

        try {
            payment.setDeletedDate(null);
            payment.setDeletedBy(null);
            paymentRepository.save(payment);
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to restore payment: " + e.getMessage(), e);
        }
        return ApiResponse.success("Payment restored successfully");
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<PaymentListResponse> search_filter(Map<String, String> allParams, String sort, String search,
            int page, int size, boolean isPage) {
        UpdatePaymentRequest paymentRequest = new UpdatePaymentRequest();
        List<PaymentGroupBy> groupByList = new ArrayList<>();
        allParams.forEach((key, value) -> {
            try {
                PaymentGroupBy groupBy = PaymentGroupBy.valueOf(key.toUpperCase());
                groupByList.add(groupBy);
                groupBy.set(paymentRequest, value);

            } catch (Exception e) {
                try {
                    switch (key.toLowerCase()) {
                        case "payment_status":
                            paymentRequest.setPaymentStatus(value);
                            break;

                        case "payment_type":
                            paymentRequest.setPaymentType(value);
                            break;

                        case "payment_method":
                            paymentRequest.setPaymentMethod(value);
                            break;

                        case "payment_timing":
                            paymentRequest.setPaymentTiming(value);
                            break;

                        case "code":
                            paymentRequest.setCode(value);
                            break;
                        case "createdate":
                        case "createddated":
                        case "created_date":
                            paymentRequest.setCreatedDate(value);
                            break;

                    }
                } catch (Exception ex) {
                    // TODO: handle exception
                    throw ex;
                }
            }
        });

        List<Sort.Order> sorts = new ArrayList<>();
        for (String s : sort.split(",")) {
            String[] str = s.split(":");
            if (str.length != 2) {
                throw new RuntimeException("Invalid sort format: field:asc|desc");
            }
            sorts.add(new Sort.Order(
                    str[1].equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                    str[0]));
        }

        Specification<PaymentEntity> spec = (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> all = new ArrayList<>();

            // --- SEARCH (OR across searchable fields) ---
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toUpperCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                // search by code only (as per your requirement)
                searchPredicates.add(cb.like(cb.upper(root.get("code")), pattern));

                all.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            // --- FILTER (AND conditions) ---
            if (paymentRequest.getPaymentStatus() != null && !paymentRequest.getPaymentStatus().isBlank()) {
                all.add(cb.equal(
                        root.get("paymentStatus"),
                        PaymentStatus.valueOf(paymentRequest.getPaymentStatus().toUpperCase().trim())));
            }

            if (paymentRequest.getPaymentType() != null && !paymentRequest.getPaymentType().isBlank()) {
                all.add(cb.equal(
                        root.get("paymentType"),
                        PaymentType.valueOf(paymentRequest.getPaymentType().toUpperCase().trim())));
            }

            if (paymentRequest.getPaymentMethod() != null && !paymentRequest.getPaymentMethod().isBlank()) {
                all.add(cb.equal(
                        root.get("paymentMethod"),
                        PaymentMethod.valueOf(paymentRequest.getPaymentMethod().toUpperCase().trim())));
            }

            if (paymentRequest.getPaymentTiming() != null && !paymentRequest.getPaymentTiming().isBlank()) {
                all.add(cb.equal(
                        root.get("paymentTiming"),
                        PaymentTiming.valueOf(paymentRequest.getPaymentTiming().toUpperCase().trim())));
            }

            if (paymentRequest.getCode() != null && !paymentRequest.getCode().isBlank()) {
                all.add(cb.like(root.get("code"), "%" + paymentRequest.getCode() + "%"));
            }
            if (paymentRequest.getCreatedDate() != null && !paymentRequest.getCreatedDate().isBlank()) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate createdDate = LocalDate.parse(paymentRequest.getCreatedDate().trim(), dateTimeFormatter);
                all.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                        createdDate.atStartOfDay()));
                all.add(cb.lessThan(root.get("createdDate"),
                        createdDate.plusDays(1).atStartOfDay()));
            }

            return all.isEmpty() ? cb.conjunction() : cb.and(all.toArray(new Predicate[0]));
        };

        Pageable pageable;
        if (isPage) {
            pageable = PageRequest.of(page - 1, size, Sort.by(sorts));
        } else {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sorts));
        }

        return ApiResponse.success(paymentListResponse(paymentRepository.findAll(spec, pageable).getContent()));
    }
}
