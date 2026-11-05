package com.veth007.hotelmanagement.model.response.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.PaymentEntity;
import com.veth007.hotelmanagement.model.response.PaymentHistoryResponse;
import com.veth007.hotelmanagement.model.response.Book.BookShortResponse;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String code;
    private String description;
    private String email;
    private BigDecimal paidAmount;
    private String currency;
    private BigDecimal refundedAmount;
    private String paymentStatus;
    private String paymentType;
    private String paymentMethod;
    private String paymentTiming;
    private String paymentGetway;
    private String getwayTransactionId;
    private LocalDateTime paymentDate;
    private String note;
    private Object book;
    private Object paymentHistory;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String deletedBy;
    private LocalDateTime deletedDate;

    public static PaymentResponse fromEntity(PaymentEntity paymentEntity) {
        PaymentResponse response = new PaymentResponse();
        response.setId(paymentEntity.getId());
        response.setCode(paymentEntity.getCode());
        response.setDescription(paymentEntity.getDescription());
        response.setEmail(paymentEntity.getEmail());
        response.setPaidAmount(paymentEntity.getPaidAmount());
        response.setCurrency(paymentEntity.getCurrency().name());
        response.setRefundedAmount(paymentEntity.getRefundedAmount());
        response.setPaymentStatus(paymentEntity.getPaymentStatus() != null ? paymentEntity.getPaymentStatus().name() : null);
        response.setPaymentType(paymentEntity.getPaymentType() != null ? paymentEntity.getPaymentType().name() : null);
        response.setPaymentMethod(paymentEntity.getPaymentMethod() != null ? paymentEntity.getPaymentMethod().name() : null);
        response.setPaymentTiming(paymentEntity.getPaymentTiming() != null ? paymentEntity.getPaymentTiming().name() : null);
        response.setPaymentGetway(paymentEntity.getPaymentGetway());
        response.setGetwayTransactionId(paymentEntity.getGetwayTransactionId());
        response.setPaymentDate(paymentEntity.getPaymentDate());
        response.setBook(paymentEntity.getBook() != null ? Map.of(
            "bookId", paymentEntity.getBook().getId(),
            "bookingCode", paymentEntity.getBook().getCode()
        ) : null);
        response.setPaymentHistory(paymentEntity.getPaymentHistories() != null
            ? paymentEntity.getPaymentHistories().stream()
                .map(PaymentHistoryResponse::fromEntity)
                .toList()
            : null);
        response.setNote(paymentEntity.getNote());
        response.setCreatedBy(paymentEntity.getCreatedBy());
        response.setCreatedDate(paymentEntity.getCreatedDate());
        response.setLastModifiedBy(paymentEntity.getLastModifiedBy());
        response.setLastModifiedDate(paymentEntity.getLastModifiedDate());
        response.setDeletedBy(paymentEntity.getDeleted_by());
        response.setDeletedDate(paymentEntity.getDeletedDate());
        return response;
    }

    public static PaymentResponse fromEntity(PaymentEntity paymentEntity, Boolean includeBook) {
        PaymentResponse response = fromEntity(paymentEntity);
        if (paymentEntity.getBook() != null && Boolean.TRUE.equals(includeBook)) {
            // include only minimal book info to avoid deep recursion / large payloads
            BookShortResponse bookResp = BookShortResponse.fromEntity(paymentEntity.getBook());
            response.setBook(bookResp);
        } else {
            response.setBook(null);
        }
        return response;
    }
}
