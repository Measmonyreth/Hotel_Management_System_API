package com.veth007.hotelmanagement.model.request.Book;

import java.io.Serializable;
import java.util.List;

import com.veth007.hotelmanagement.model.request.BookItems.BookItemsRequest;
import com.veth007.hotelmanagement.model.request.Discount.DiscountRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class BookRequest implements Serializable {
    @NotBlank(message = "Customer ID is required")
    private Long customerId;
    // @NotBlank(message = "Employee ID is required")
    private Long employeeId;

    @Valid
    @NotEmpty(message = "At least one book item is required")
    @Size(max = 2, message = "You can book at most two items")
    private List<BookItemsRequest> bookItemsRequest;
    @NotBlank(message = "Booking type is required")
    private String bookingType;    
    @NotBlank(message = "Check in date is required")
    private String checkInDate;
    @NotBlank(message = "Check out date is required")
    private String checkOutDate;

    private DiscountRequest discountRequest;
}
