package com.veth007.hotelmanagement.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.CurrencyType;
import com.veth007.hotelmanagement.constant.enums.PaymentMethod;
import com.veth007.hotelmanagement.constant.enums.PaymentTiming;
import com.veth007.hotelmanagement.constant.enums.PaymentType;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@Entity(name = "payment_history")
@Table(name = "payment_history")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class PaymentHistory extends BaseSoftDeleted<Long> {

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_triming")
    private PaymentTiming paymentTiming;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private CurrencyType currency;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_getway")
    private String paymentGetway;

    @Column(name = "getway_transaction_id")
    private String getwayTransactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "notes")
    private String notes;
}
