package com.veth007.hotelmanagement.model.request.Book;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

import com.veth007.hotelmanagement.model.request.BookItems.UpdateBookItemsRequest;
import com.veth007.hotelmanagement.model.request.CustomerRequest.UpdateCustomerRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor 
@NoArgsConstructor
public class UpdateBookRequest implements Serializable {
    private String code;
    private String payment_status;
    private String createDated;
    private UpdateCustomerRequest updateCustomerRequest;
    private UpdateBookItemsRequest updateBookItemsRequest;
}
