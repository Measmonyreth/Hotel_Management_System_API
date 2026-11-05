package com.veth007.hotelmanagement.model.request.Payment;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentRequest implements Serializable {
    private String code;
    private String description;
    private Double discountAmount;
    private Double paidAmount;
    private String paymentStatus;
    private String paymentType;
    private String paymentMethod;
    private String paymentTiming;
    private LocalDateTime paymentDate;
    private String note;
    private Object book;
    private String createdBy;
    private String createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String deletedBy;
    private LocalDateTime deletedDate;
}
