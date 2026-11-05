package com.veth007.hotelmanagement.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.PaymentHistory;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentShortResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentHistoryResponse {
    private long id;
    private Object payment;
    private String paymentType;
    private String paymentMethod;
    private String paymentTiming;
    private String currency;
    private BigDecimal amount;
    private String paymentGetway;
    private String getwayTransactionId;
    private String paymentDate;
    private String notes;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String deletedBy;
    private LocalDateTime deletedDate;

    public static PaymentHistoryResponse fromEntity(PaymentHistory paymentHistoryEntity) {
        return PaymentHistoryResponse.builder()
                .id(paymentHistoryEntity.getId())
                .payment(PaymentShortResponse.fromEntity(paymentHistoryEntity.getPayment()))
                .paymentType(paymentHistoryEntity.getPaymentType().name())
                .paymentMethod(paymentHistoryEntity.getPaymentMethod().name())
                .paymentTiming(paymentHistoryEntity.getPaymentTiming() != null
                    ? paymentHistoryEntity.getPaymentTiming().name() : null)
                .currency(paymentHistoryEntity.getCurrency().name())
                .amount(paymentHistoryEntity.getAmount())
                .paymentGetway(paymentHistoryEntity.getPaymentGetway())
                .getwayTransactionId(paymentHistoryEntity.getGetwayTransactionId())
                .paymentDate(paymentHistoryEntity.getPaymentDate().toString())
                .notes(paymentHistoryEntity.getNotes())
                
                .createdBy(paymentHistoryEntity.getCreatedBy())
                .createdDate(paymentHistoryEntity.getCreatedDate())
                .lastModifiedBy(paymentHistoryEntity.getLastModifiedBy())
                .lastModifiedDate(paymentHistoryEntity.getLastModifiedDate())
                .deletedBy(paymentHistoryEntity.getDeleted_by())
                .deletedDate(paymentHistoryEntity.getDeletedDate())
                .build();
    }
}
