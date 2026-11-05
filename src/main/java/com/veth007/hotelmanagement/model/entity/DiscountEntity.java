package com.veth007.hotelmanagement.model.entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.DiscountType;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

@Table(name = "discount")
@Entity(name = "discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class DiscountEntity extends BaseSoftDeleted<Long> {
   
    @Column(nullable = false)
    private String code;               // e.g. "VIP-GOLD-2025"

    @Column(nullable = false)
    private String description;        // e.g. "Gold VIP 15% discount"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type;         // PERCENTAGE, FIXED_AMOUNT etc.

    @Column(nullable = false)
    private BigDecimal discount_price; 

    // no cascade: VIP cards are independent aggregates; CascadeType.ALL can corrupt/abort TXs on save
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_vip", nullable = true)
    private VipCustomerEntity vip;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "discount")
    private BookItemEntity book;
}
