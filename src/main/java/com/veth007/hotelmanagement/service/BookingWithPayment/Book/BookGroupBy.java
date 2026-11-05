package com.veth007.hotelmanagement.service.BookingWithPayment.Book;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;
import com.veth007.hotelmanagement.model.request.Book.UpdateBookRequest;
import com.veth007.hotelmanagement.model.response.Book.BookResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookGroupBy {
    PAYMENT_STATUS(dto -> dto.getPayment_status() != null ? dto.getPayment_status() : "N/A",
            (filter, value) -> filter.setPayment_status(value)),
    BOOK_STATUS(dto -> dto.getBook_status() != null ? dto.getBook_status().name() : "N/A",
            (filter, value) -> filter.getUpdateBookItemsRequest().setStatus(value)),

    CREATED_DATE(dto -> dto.getCreatedDate() != null ?
            dto.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
            (filter, value) -> filter.setCreateDated(value));

    private final Function<BookResponse, String> exractor;
    private final BiConsumer<UpdateBookRequest, String> setter;

    public String extract(BookResponse book) {
        return this.exractor.apply(book);
    }

    public void set(UpdateBookRequest book, String value) {
       setter.accept(book, value);
    }

}
