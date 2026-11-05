package com.veth007.hotelmanagement.service.BookingWithPayment.Book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.BookItemEntity;
import com.veth007.hotelmanagement.model.request.Book.BookRequest;
import com.veth007.hotelmanagement.model.request.Book.UpdateBookRequest;
import com.veth007.hotelmanagement.model.response.Book.BookItemResponse;
import com.veth007.hotelmanagement.model.response.Book.BookListReponse;
import com.veth007.hotelmanagement.model.response.Book.BookResponse;

public interface BookService {
    BookItemEntity createBooking(BookRequest request);
    BigDecimal quotePrice(Long roomId, Long customerId, String checkInDate, String checkOutDate);
    ApiResponse<BookResponse> checkIn(Long bookItemId);
    ApiResponse<BookResponse> checkOut(Long bookItemId);
    ApiResponse<BookResponse> complete(Long bookItemId);
    ApiResponse<Map<String, Object>> cancel_book_item(long item_id);
    ApiResponse<Map<String, Object>> cancel_booking(long book_id);
    ApiResponse<BookListReponse> getAll();
    ApiResponse<BookResponse> getByCustomer(Long cus_id);
    ApiResponse<BookResponse> get_byId(Long id);
    ApiResponse<BookResponse> get_history_book_cus(Long cus_id);
    ApiResponse<List<BookResponse>> get_byCustomer(String customerUsername);
    ApiResponse<String> delete_byId(Long id);
    ApiResponse<BookListReponse> search_filter(Map<String, String> allParams, UpdateBookRequest filter, String sort, int page, int size, boolean isPage, String search);
}
