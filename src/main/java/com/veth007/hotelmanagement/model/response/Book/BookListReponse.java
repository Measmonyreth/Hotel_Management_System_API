package com.veth007.hotelmanagement.model.response.Book;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookListReponse {
    private int totalBooking;
    private int totalCancel;
    private int totalPaid;
    private int totalPartially_paid;
    private int totalUnpaid;
    private int totalPartially_refund;
    private int totalRefund;
    private List<BookResponse> bookResponses;
}
