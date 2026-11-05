package com.veth007.hotelmanagement.model.entity;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.CurrencyType;
import com.veth007.hotelmanagement.constant.enums.PaymentMethod;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.constant.enums.PaymentTiming;
import com.veth007.hotelmanagement.constant.enums.PaymentType;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;

@Table(name = "payments")
@Entity(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)

public class PaymentEntity extends BaseSoftDeleted<Long> {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "refunded_amount")
    private BigDecimal refundedAmount;   // null until a refund happens

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private CurrencyType currency;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentTiming paymentTiming;

    @Column(name = "payment_getway")
    private String paymentGetway;

    @Column(name = "getway_transaction_id")
    private String getwayTransactionId;


    @Column(name = "payment_date")
    private java.time.LocalDateTime paymentDate;    
    
    @Column(name = "note")
    private String note;



    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private List<PaymentHistory> paymentHistories;
}
