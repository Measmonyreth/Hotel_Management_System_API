package com.veth007.hotelmanagement.model.response.Book;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.constant.enums.BookingType;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.BookItemEntity;
import com.veth007.hotelmanagement.model.response.Room.RoomResponse;
import com.veth007.hotelmanagement.model.response.Room.RoomShortResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class BookItemResponse {
    private Long id;
    private Object room;
    private Object book;
    private String booking_type;
    private BigDecimal unit_price;
    private BigDecimal discount_amount;
    private BigDecimal total_amount;
    private Object discount;
     private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;

    // how to fetch only id and name of room instead of whole object
    private static BigDecimal round(BigDecimal value) {
        if (value == null) return null;
        return BigDecimal.valueOf(value.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    public static BookItemResponse fromEntity(BookItemEntity bookItemEntity, Boolean includeRoom) {
        return BookItemResponse.builder()
                .id(bookItemEntity.getId())
                .room(bookItemEntity.getRoom() != null
                        ? (Boolean.TRUE.equals(includeRoom)
                                ? RoomResponse.fromEntity(bookItemEntity.getRoom())
                                : RoomShortResponse.fromEntity(bookItemEntity.getRoom()))
                        : null)
                .book(bookItemEntity.getBook() != null
                        ? HashIdUtil.encodeId(bookItemEntity.getBook().getId())
                        : null)
                .booking_type(bookItemEntity.getBook() != null && bookItemEntity.getBook().getBookingType() != null
                        ? bookItemEntity.getBook().getBookingType().name()
                        : null)
                .unit_price(bookItemEntity.getUnitPrice())
                .discount_amount(round(bookItemEntity.getDiscountAmount()))
                .total_amount(round(bookItemEntity.getTotalAmount()))
                .discount(bookItemEntity.getDiscount() != null
                        ? Map.of("id", HashIdUtil.encodeId(bookItemEntity.getDiscount().getId()), "code", bookItemEntity.getDiscount().getCode())
                        : null)
                .createdDate(bookItemEntity.getCreatedDate())
                .lastModifiedDate(bookItemEntity.getLastModifiedDate())
                .createBy(bookItemEntity.getCreatedBy())
                .lastModifiedBy(bookItemEntity.getLastModifiedBy())
                .deletedBy(bookItemEntity.getDeleted_by())
                .build();
    }
}
