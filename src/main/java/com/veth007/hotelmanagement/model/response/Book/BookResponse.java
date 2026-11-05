package com.veth007.hotelmanagement.model.response.Book;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.model.entity.BookEntity;
import com.veth007.hotelmanagement.model.entity.BookItemEntity;
import com.veth007.hotelmanagement.model.response.Customer.CustomerResponse;
import com.veth007.hotelmanagement.model.response.Customer.CustomerShortResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeResponse;
import com.veth007.hotelmanagement.model.response.Employee.EmployeeShortResponse;
import com.veth007.hotelmanagement.model.response.PaymentResponse.PaymentResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class BookResponse {
        private Long id;
        private String code;
        private Object customer;
        private Object employee;
        private BookStatus book_status;
        private int totalBooking;
        private String payment_status;
        private BigDecimal remainingAmount;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal sub_totalAmount;
        private BigDecimal discountAmount;
        private int quantity_night;
        private LocalDateTime checkInDate;
        private LocalDateTime checkOutDate;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private LocalDateTime deletedDate;
        private String createBy;
        private String lastModifiedBy;
        private String deletedBy;
        private int totalBookitem;
        private List<BookItemResponse> bookItems;
        private List<PaymentResponse> payment;

        // how to fetch only id and name of customer and employee instead of whole
        // object
        public static BookResponse fromEntity(BookEntity bookEntity, Boolean includeCustomer, Boolean includeEmployee) {
                return BookResponse.builder()
                                .id(bookEntity.getId())
                                .code(bookEntity.getCode())
                                .customer(bookEntity.getCustomer() != null
                                                ? (Boolean.TRUE.equals(includeCustomer)
                                                                ? CustomerResponse.fromEntity(bookEntity.getCustomer())
                                                                : CustomerShortResponse
                                                                                .fromEntity(bookEntity.getCustomer()))
                                                : null)
                                .employee(bookEntity.getEmployee() != null
                                                ? (Boolean.TRUE.equals(includeEmployee)
                                                                ? EmployeeResponse.fromEntity(bookEntity.getEmployee())
                                                                : EmployeeShortResponse
                                                                                .fromEntity(bookEntity.getEmployee()))
                                                : null)
                                .totalBooking(bookEntity.getTotalBookingItems())
                                .book_status(bookEntity.getBookStatus() != null ? bookEntity.getBookStatus() : null)
                                .payment_status(bookEntity.getPaymentStatus() != null
                                                ? bookEntity.getPaymentStatus().name()
                                                : null)
                                .totalAmount(bookEntity.getTotalAmount())
                                .sub_totalAmount(bookEntity.getSub_totalAmount())
                                .discountAmount(bookEntity.getDiscountAmount())
                                .quantity_night(bookEntity.getQuantity_night() != null ? bookEntity.getQuantity_night() : 0)
                                .checkInDate(bookEntity.getCheckInDate())
                                .checkOutDate(bookEntity.getCheckOutDate())
                                .remainingAmount(bookEntity.getRemainingAmount())
                                .paidAmount(bookEntity.getPaidAmount())
                                .totalBookitem(bookEntity.getBookItems() != null ? bookEntity.getBookItems().size() : 0)
                                .bookItems(bookEntity.getBookItems() != null
                                                ? bookEntity.getBookItems().stream()
                                                                .map(item -> BookItemResponse.fromEntity(item, false))
                                                                .collect(Collectors.toList())
                                                : null)
                                .payment(bookEntity.getPayments() != null
                                                ? bookEntity.getPayments().stream()
                                                                .map(payment -> PaymentResponse.fromEntity(payment,
                                                                                false))
                                                                .collect(Collectors.toList())
                                                : null)
                                .createdDate(bookEntity.getCreatedDate())
                                .lastModifiedDate(bookEntity.getLastModifiedDate())
                                .deletedDate(bookEntity.getDeletedDate())
                                .createBy(bookEntity.getCreatedBy())
                                .lastModifiedBy(bookEntity.getLastModifiedBy())
                                .deletedBy(bookEntity.getDeleted_by())

                                .build();
        }

        public static BookResponse fromEntity(BookEntity bookEntity) {
                return fromEntity(bookEntity, false, false);
        }

        public static BookResponse fromEntity(BookEntity bookEntity, BookItemEntity newItem) {
                BookResponse response = fromEntity(bookEntity, false, false);
                if (newItem != null && (response.getBookItems() == null
                                || response.getBookItems().stream()
                                                .noneMatch(item -> Objects.equals(item.getId(), newItem.getId())))) {
                        List<BookItemResponse> items = response.getBookItems() != null
                                        ? new ArrayList<>(response.getBookItems())
                                        : new ArrayList<>();
                        items.add(BookItemResponse.fromEntity(newItem, false));
                        response.setBookItems(items);
                }
                return response;
        }
}
