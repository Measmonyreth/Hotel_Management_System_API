package com.veth007.hotelmanagement.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.constant.enums.BookingType;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Table(name = "bookItems")
@Entity(name = "bookItem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class BookItemEntity extends BaseSoftDeleted<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_room")
    private RoomEntity room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_book")
    private BookEntity book;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_discount")
    private DiscountEntity discount;

}
