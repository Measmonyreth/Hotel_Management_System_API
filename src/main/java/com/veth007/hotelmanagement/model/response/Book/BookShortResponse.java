package com.veth007.hotelmanagement.model.response.Book;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.BookEntity;
import com.veth007.hotelmanagement.model.response.Customer.CustomerShortResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeShortResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookShortResponse implements Serializable {
    private Long id;
    private String code;
    private Object customer;
    private Object employee;
    private int totalBookingItems;
    private BigDecimal totalAmount;
    private BigDecimal sub_totalAmount;
    private BigDecimal discountAmount;
    private int quantity_night;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;

    public static BookShortResponse fromEntity(BookEntity bookEntity) {
        return BookShortResponse.builder()
                .id(bookEntity.getId())
                .code(bookEntity.getCode())
                .customer(CustomerShortResponse.fromEntity(bookEntity.getCustomer()))

                .employee(EmployeeShortResponse
                        .fromEntity(bookEntity.getEmployee()))

                .totalBookingItems(bookEntity.getTotalBookingItems())
                .totalAmount(bookEntity.getTotalAmount())
                .sub_totalAmount(bookEntity.getSub_totalAmount())
                .discountAmount(bookEntity.getDiscountAmount())
                .quantity_night(bookEntity.getQuantity_night())
                .paidAmount(bookEntity.getPaidAmount())
                .remainingAmount(bookEntity.getRemainingAmount())
                .checkInDate(bookEntity.getCheckInDate())
                .checkOutDate(bookEntity.getCheckOutDate()).build();
    }
}
