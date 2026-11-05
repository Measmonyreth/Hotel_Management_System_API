package com.veth007.hotelmanagement.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.PaymentTiming;
import com.veth007.hotelmanagement.constant.enums.RefundStatus;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "cancel_requests")
@Entity(name = "cancelRequest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CancelRequestEntity extends BaseSoftDeleted<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_book", nullable = false)
    private BookEntity book;

    @Column(name = "reason", nullable = false)
    private String reason; // Why the guest is cancelling

    @Column(name = "calculated_penalty_fee")
    private BigDecimal calculatedPenaltyFee; // What the system suggested

    @Column(name = "calculated_refund_amount")
    private BigDecimal calculatedRefundAmount; // What the system suggested

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus status; // PENDING, APPROVED, REJECTED

    @Enumerated(EnumType.STRING)
    @Column(name = "timing", nullable = true)
    private PaymentTiming timing; 

    @Column(name = "reviewed_by", nullable = true)
    private String reviewedBy; // Employee username/ID who approved/rejected it

    @Column(name = "rejection_reason", nullable = true)
    private String rejectionReason; // Filled out only if status is REJECTED

    @Column(name = "reviewed_date", nullable = true)
    private LocalDateTime reviewedDate;

}
