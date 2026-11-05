package com.veth007.hotelmanagement.model.response.Cancel;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import com.veth007.hotelmanagement.model.entity.CancelRequestEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelResponse implements Serializable {
    private Long id;
    private Object bookId;
   // private Object paymentId;
    private String reason; // Why the guest is cancelling
    private String calculatedPenaltyFee; // What the system suggested
    private String calculatedRefundAmount; // What the system suggested
    private String status; // Status of the cancellation (e.g., "Cancelled", "Refunded", etc.)
    private String reviewedBy;
    private String rejectionReason; // Reason for rejection, if applicable
    private LocalDateTime reviewedDate ; // Employee username/ID who approved/rejected it
        private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;


    public static CancelResponse fromEntity(CancelRequestEntity entity) {
        return CancelResponse.builder()
        .id(entity.getId())
                .bookId(entity.getBook()!= null ? Map.of(
                    "id", entity.getBook().getId(),
                    "code", entity.getBook().getCode()
                ):null)
        //        .paymentId(entity.getPayment().getId())
                .reason(entity.getReason())
                .calculatedPenaltyFee(entity.getCalculatedPenaltyFee() != null ? entity.getCalculatedPenaltyFee().toString() : null)
                .calculatedRefundAmount(entity.getCalculatedRefundAmount() != null ? entity.getCalculatedRefundAmount().toString() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .reviewedBy(entity.getReviewedBy())
                .rejectionReason(entity.getRejectionReason())
                .reviewedDate(entity.getReviewedDate())
                 .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .deletedDate(entity.getDeletedDate())
                .createBy(entity.getCreatedBy())
                .lastModifiedBy(entity.getLastModifiedBy())
                .deletedBy(entity.getDeleted_by())
                .build();
    }
}
