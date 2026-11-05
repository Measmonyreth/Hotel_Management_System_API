package com.veth007.hotelmanagement.service.BookingWithPayment.Book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.rpc.context.AttributeContext.Auth;
import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.constant.enums.BookingType;
import com.veth007.hotelmanagement.constant.enums.DiscountType;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.StatusRoom;
import com.veth007.hotelmanagement.constant.enums.VipStatus;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.BusinessException;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.BookEntity;
import com.veth007.hotelmanagement.model.entity.BookItemEntity;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.DiscountEntity;
import com.veth007.hotelmanagement.model.entity.EmployeeDetails;
import com.veth007.hotelmanagement.model.entity.EmployeeEntity;
import com.veth007.hotelmanagement.model.entity.FcmTokenEntity;
import com.veth007.hotelmanagement.model.entity.PaymentEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.request.Book.BookRequest;
import com.veth007.hotelmanagement.model.request.Book.UpdateBookRequest;
import com.veth007.hotelmanagement.model.response.Book.BookItemResponse;
import com.veth007.hotelmanagement.model.response.Book.BookListReponse;
import com.veth007.hotelmanagement.model.response.Book.BookResponse;
import com.veth007.hotelmanagement.repository.BookItemRepository;
import com.veth007.hotelmanagement.repository.BookRepository;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.DiscountRepository;
import com.veth007.hotelmanagement.repository.EmployeeRepository;
import com.veth007.hotelmanagement.repository.FcmTokenRepository;
import com.veth007.hotelmanagement.repository.RoomRepository;
import com.veth007.hotelmanagement.repository.VipCustomerRepository;
import com.veth007.hotelmanagement.service.notification.NotificationService;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookServiceImp implements BookService {
    private final BookRepository bookRepository;
    private final BookItemRepository bookItemRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final VipCustomerRepository vipCustomerRepository;
    private final DiscountRepository discountRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public BookItemEntity createBooking(BookRequest request) {
        if (request == null || request.getBookItemsRequest() == null || request.getBookItemsRequest().isEmpty())
            throw new IllegalArgumentException("At least one book item is required");
        if (request.getBookItemsRequest().size() > 2)
            throw new IllegalArgumentException("A booking can contain at most two items");
        if (request.getCustomerId() == null)
            throw new IllegalArgumentException("Customer ID is required");

        LocalDateTime checkIn = parseBookingDateTime(request.getCheckInDate());
        LocalDateTime checkOut = parseBookingDateTime(request.getCheckOutDate());
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Check-out must be after check-in");
        for (var item : request.getBookItemsRequest()) {
            if (item == null || item.getRoomId() == null || request.getBookingType() == null)
                throw new IllegalArgumentException("Each item requires a room ID and booking type");
            validateRoom(item.getRoomId(), checkIn.toLocalDate(), checkOut.toLocalDate());
        }
        validateCustomer(request.getCustomerId());

        BookEntity book = CreateBooking(request);
        BookItemEntity lastItem = null;
        for (var item : request.getBookItemsRequest())
            lastItem = addBookItemToBooking(book, request, item);
        return lastItem;
    }

    private BookEntity CreateBooking(BookRequest request) {
        Long customerId = request.getCustomerId();

        BookEntity latestBookOpt = this.bookRepository
                .findFirstByCustomerIdAndDeletedDateIsNullOrderByCreatedDateDesc(customerId);

        if (latestBookOpt == null) {
            return createNewBooking(request);
        }

        // BookEntity latestBook = latestBookOpt;

        // // Find the most recent payment for that booking (if any)
        // PaymentEntity latestPayment = null;
        // var payments = latestBook.getPayments();
        // if (payments != null && !payments.isEmpty()) {
        // latestPayment = payments.stream()
        // .filter(Objects::nonNull)
        // .max(Comparator.comparing(p -> p.getCreatedDate() != null ?
        // p.getCreatedDate() : LocalDateTime.MIN))
        // .orElse(null);
        // }

        // // Reuse booking only if there is no payment yet, or it's unpaid/partially
        // paid
        // if (latestPayment == null || latestPayment.getPaymentStatus() ==
        // PaymentStatus.UNPAID
        // || latestPayment.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID) {
        // return latestBook;
        // }

        // PAID, REFUNDED, PARTIALLY_REFUNDED (or other finalized statuses) → create new
        // booking
        return createNewBooking(request);

        // throw new RuntimeException("Cannot add items to booking with status: "
        // + latestPayment.getPaymentStatus());
    }

    private BookEntity createNewBooking(BookRequest request) {
        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        // employee is optional for online bookings made from the app
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EmployeeEntity employee = null;
        if (auth != null && auth.getPrincipal() instanceof EmployeeDetails) {
            EmployeeDetails userDetails = (EmployeeDetails) auth.getPrincipal();
            employee = userDetails.getEmployee();
        }

        BookEntity book = new BookEntity();
        book.setBookStatus(BookStatus.PENDING);
        book.setCode(generateBookingCode());
        book.setCheckInDate(parseBookingDateTime(request.getCheckInDate()));
        book.setCheckOutDate(parseBookingDateTime(request.getCheckOutDate()));
        book.setQuantity_night((int) ChronoUnit.DAYS.between(book.getCheckInDate().toLocalDate(),
                book.getCheckOutDate().toLocalDate()));
        book.setCustomer(customer);
        book.setEmployee(employee);
        book.setBookingType(BookingType.valueOf(request.getBookingType()));
        return bookRepository.save(book);
    }

    private BookItemEntity addBookItemToBooking(BookEntity book, BookRequest request,
            com.veth007.hotelmanagement.model.request.BookItems.BookItemsRequest itemRequest) {

        // // ✅ Guard: double-check status before adding
        // if (book.getPayment().getPaymentStatus() == PaymentStatus.PAID ||
        // book.getPayment().getPaymentStatus() == PaymentStatus.REFUNDED) {
        // throw new RuntimeException("Cannot add items to a completed booking. Please
        // create a new booking.");
        // }

        var room = roomRepository.findById(itemRequest.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Calculate nights
        LocalDateTime checkIn = parseBookingDateTime(request.getCheckInDate());
        LocalDateTime checkOut = parseBookingDateTime(request.getCheckOutDate());
        int nights = (int) ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (nights <= 0)
            throw new RuntimeException("Check-out must be after check-in");

        BigDecimal unitPrice = room.getPrice();
        BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP); // subtotal
                                                                                                              // before
                                                                                                              // discount
        // VIP discount
        BigDecimal discountPrice = BigDecimal.ZERO;
        BigDecimal vipDiscount = BigDecimal.ZERO;
        BigDecimal roomDiscount = BigDecimal.ZERO;
        DiscountEntity discount = null;

        var vipCard = vipCustomerRepository.findByCustomerId(request.getCustomerId()).orElse(null);
        if (vipCard != null && vipCard.getStatus() == VipStatus.ACTIVE) {
            vipDiscount = amount
                    .multiply(BigDecimal.valueOf(vipCard.getPercentage_discount()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            discountPrice = discountPrice.add(vipDiscount);
        } else {
            vipCard = null; // inactive card must not be linked to the discount
        }
        if (room.getDiscountPrice() != null) {
            roomDiscount = room.getDiscountPrice()
                    .multiply(BigDecimal.valueOf(nights))
                    .setScale(2, RoundingMode.HALF_UP);
            discountPrice = discountPrice.add(roomDiscount);
        }

        // Save discount with the combined price (VIP + room), if any discount applies
        if (discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            DiscountType appliedType = resolveDiscountType(roomDiscount, vipDiscount);
            // validateDiscountType(request.getDiscountRequest().getType(), appliedType);

            discount = new DiscountEntity();
            discount.setVip(vipCard);
            discount.setCode(book.getCode() + "-Discount-" + (book.getTotalBookingItems() + 1));
            discount.setDescription(request.getDiscountRequest() == null
                    ? "Booking discount"
                    : request.getDiscountRequest().getDescription());
            discount.setType(appliedType);
            discount.setDiscount_price(discountPrice);

            discount = discountRepository.save(discount);
        }
        BigDecimal totalAmount = amount.subtract(discountPrice)
                .setScale(2, RoundingMode.HALF_UP);

        // Update book totals
        BigDecimal currentTotal = book.getTotalAmount() != null ? book.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal currentDiscount = book.getDiscountAmount() != null
                ? book.getDiscountAmount()
                : BigDecimal.ZERO;
        BigDecimal currentSubtotal = book.getSub_totalAmount() != null
                ? book.getSub_totalAmount()
                : currentTotal.add(currentDiscount);
        BigDecimal paidAmount = book.getPaidAmount() != null ? book.getPaidAmount() : BigDecimal.ZERO;

        BigDecimal newTotal = currentTotal.add(totalAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newSubtotal = currentSubtotal.add(amount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newDiscount = currentDiscount.add(discountPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = newTotal.subtract(paidAmount).setScale(2, RoundingMode.HALF_UP);

        book.setTotalAmount(newTotal);
        book.setSub_totalAmount(newSubtotal);
        book.setDiscountAmount(newDiscount);
        book.setRemainingAmount(remaining);
        book.setPaidAmount(paidAmount); // keep the existing paid amount
        book.setTotalBookingItems(book.getTotalBookingItems() + 1);
        book.setPaymentStatus(book.getPaymentStatus() != null ? book.getPaymentStatus() : PaymentStatus.UNPAID);
        book.setBookingType(BookingType.valueOf(request.getBookingType()));
        bookRepository.save(book);

        // Save book item
        BookItemEntity bookItem = new BookItemEntity();
        bookItem.setBook(book);
        bookItem.setRoom(room);
        bookItem.setUnitPrice(unitPrice); // price per night
        bookItem.setDiscountAmount(discountPrice);
        bookItem.setTotalAmount(totalAmount); // amount - discount

        // Set status with null-safety check
     //   String statusStr = request.getStatus();
        bookItem.setDiscount(discount);
        BookItemEntity saved = bookItemRepository.save(bookItem);

        room.setStatusRoom(StatusRoom.BOOKED);
        roomRepository.save(room);

        return saved;
    }

    private DiscountType resolveDiscountType(BigDecimal roomDiscount, BigDecimal vipDiscount) {
        boolean hasRoomDiscount = roomDiscount.compareTo(BigDecimal.ZERO) > 0;
        boolean hasVipDiscount = vipDiscount.compareTo(BigDecimal.ZERO) > 0;

        if (hasRoomDiscount && hasVipDiscount)
            return DiscountType.GENERAL_AND_CARD;
        return hasVipDiscount ? DiscountType.CARD : DiscountType.GENERAL;
    }

    // private void validateDiscountType(String requestedType, DiscountType
    // appliedType) {
    // DiscountType requested;
    // try {
    // requested = DiscountType.valueOf(requestedType);
    // } catch (IllegalArgumentException | NullPointerException e) {
    // throw new RuntimeException("Invalid discount type: " + requestedType
    // + ". Allowed values: GENERAL, CARD, GENERAL_AND_CARD");
    // }
    // if (requested != appliedType) {
    // throw new RuntimeException("Discount type mismatch: request has " + requested
    // + " but the applied discounts are " + appliedType
    // + " (GENERAL = room discount, CARD = VIP card, GENERAL_AND_CARD = both)");
    // }
    // }

    private void validateRoom(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (roomRepository.existsByIdAndDeletedDateIsNotNull(roomId)) {
            throw new NotFoundException("Room is deleted");
        }

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        // only operational states block a room; BOOKED rooms can still be
        // booked for other, non-overlapping dates
        StatusRoom status = room.getStatusRoom();
        if (status == StatusRoom.MAINTENANCE || status == StatusRoom.CLEANING
                || status == StatusRoom.OUT_OF_SERVICE) {
            throw new NotFoundException("Room is not available: " + status);
        }

        List<BookStatus> excluded = List.of(BookStatus.CANCELLED, BookStatus.CHECKED_OUT, BookStatus.COMPLETED);
        List<PaymentStatus> activePayments = List.of(
                PaymentStatus.PAID, PaymentStatus.PARTIALLY_PAID, PaymentStatus.PARTIALLY_REFUNDED);
        // if (bookItemRepository.existsOverlappingBooking(roomId,
        // checkIn.atStartOfDay(), checkOut.atStartOfDay(), excluded, activePayments)) {
        // throw new AlreadyExistException("Room is already booked for the selected
        // dates");
        // }
    }

    static LocalDateTime parseBookingDateTime(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Booking date is required");
        }

        String normalized = value.trim();
        try {
            if (normalized.length() == 10) {
                return LocalDate.parse(normalized).atStartOfDay();
            }
            return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid booking date. Expected yyyy-MM-dd or an ISO local date-time", e);
        }
    }

    // price quote for a booking that does not exist yet (pay-first flow):
    // same pricing rules as addBookItemToBooking, but nothing is persisted
    @Override
    @Transactional(readOnly = true)
    public BigDecimal quotePrice(Long roomId, Long customerId, String checkInDate, String checkOutDate) {
        validateCustomer(customerId);

        LocalDate checkIn = parseBookingDateTime(checkInDate).toLocalDate();
        LocalDate checkOut = parseBookingDateTime(checkOutDate).toLocalDate();
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0)
            throw new RuntimeException("Check-out must be after check-in");

        validateRoom(roomId, checkIn, checkOut);

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        BigDecimal amount = room.getPrice().multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountPrice = BigDecimal.ZERO;

        var vipCard = vipCustomerRepository.findByCustomerId(customerId).orElse(null);
        if (vipCard != null && vipCard.getStatus() == VipStatus.ACTIVE) {
            discountPrice = discountPrice.add(amount
                    .multiply(BigDecimal.valueOf(vipCard.getPercentage_discount()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }
        if (room.getDiscountPrice() != null) {
            discountPrice = discountPrice.add(room.getDiscountPrice()
                    .multiply(BigDecimal.valueOf(nights))
                    .setScale(2, RoundingMode.HALF_UP));
        }

        return amount.subtract(discountPrice).setScale(2, RoundingMode.HALF_UP);
    }

    // ─── Booking lifecycle: check-in → check-out → completed ───

    @Override
    @Transactional
    public ApiResponse<BookResponse> checkIn(Long bookId) {
        BookEntity book = getActiveBooking(bookId);

        if (book.getBookStatus() != BookStatus.PENDING && book.getBookStatus() != BookStatus.CONFIRMED) {
            throw new BusinessException("Cannot check in a booking with status: " + book.getBookStatus());
        }

        PaymentStatus paymentStatus = book.getPaymentStatus();
        if (paymentStatus == null || paymentStatus == PaymentStatus.UNPAID
                || paymentStatus == PaymentStatus.PARTIALLY_PAID
                || paymentStatus == PaymentStatus.REFUNDED) {
            throw new BusinessException("Cannot check in: booking payment status is "
                    + (paymentStatus == null ? PaymentStatus.UNPAID : paymentStatus)
                    + ". A deposit or full payment is required.");
        }
        List<BookItemEntity> bookingItems = book.getBookItems();
        if (bookingItems == null || bookingItems.isEmpty()) {
            throw new BusinessException("Cannot check in: booking has no room items.");
        }

        List<BookItemEntity> checkedInItems = bookingItems;

        book.setBookStatus(BookStatus.CHECKED_IN);
        for (BookItemEntity item : checkedInItems) {
            RoomEntity room = item.getRoom();
            if (room != null) {
                room.setStatusRoom(StatusRoom.OCCUPIED);
                roomRepository.save(room);
            }
        }
        bookRepository.save(book);

        Optional<FcmTokenEntity> fcmTokenEntity = fcmTokenRepository
                .findFirstByCustomer_IdAndIsActiveTrueOrderByIdDesc(book.getCustomer().getId());
        {
            String roomNumbers = checkedInItems.stream()
                    .map(BookItemEntity::getRoom)
                    .filter(Objects::nonNull)
                    .map(RoomEntity::getRoomNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            notificationService.bookingCheckin(book.getCustomer().getId(), book.getId(),
                    roomNumbers,
                    fcmTokenEntity.map(FcmTokenEntity::getToken).orElse(null));
        }

        return ApiResponse.success(BookResponse.fromEntity(book));
    }

    @Override
    @Transactional
    public ApiResponse<BookResponse> checkOut(Long bookId) {
        BookEntity book = getActiveBooking(bookId);

        if (book.getBookStatus() != BookStatus.CHECKED_IN) {
            throw new BusinessException("Cannot check out a booking with status: " + book.getBookStatus()
                    + ". Only CHECKED_IN bookings can be checked out.");
        }
 
        List<BookItemEntity> bookingItems = book.getBookItems();
        if (bookingItems == null || bookingItems.isEmpty()) {
            throw new BusinessException("Cannot check out: booking has no room items.");
        }

        book.setBookStatus(BookStatus.CHECKED_OUT);
        for (BookItemEntity item : bookingItems) {
            RoomEntity room = item.getRoom();
            if (room != null) {
                room.setStatusRoom(StatusRoom.WAIT_CLEAN);
                roomRepository.save(room);
            }
        }
        BookEntity saved = bookRepository.save(book);
        Optional<FcmTokenEntity> fcmTokenEntity = fcmTokenRepository
                .findFirstByCustomer_IdAndIsActiveTrueOrderByIdDesc(book.getCustomer().getId());
        {
            String roomNumbers = bookingItems.stream()
                    .map(BookItemEntity::getRoom)
                    .filter(Objects::nonNull)
                    .map(RoomEntity::getRoomNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            notificationService.bookingCheckout(book.getCustomer().getId(), book.getId(),
                    roomNumbers,
                    fcmTokenEntity.map(FcmTokenEntity::getToken).orElse(null));
        }

        return ApiResponse.success(BookResponse.fromEntity(saved));
    }

    @Override
    @Transactional
    public ApiResponse<BookResponse> complete(Long bookId) {
        BookEntity book = getActiveBooking(bookId);

        if (book.getBookStatus() != BookStatus.CHECKED_OUT) {
            throw new BusinessException("Cannot complete a booking with status: " + book.getBookStatus()
                    + ". Only CHECKED_OUT bookings can be completed.");
        }

        PaymentStatus paymentStatus = book.getPaymentStatus();
        if (paymentStatus != PaymentStatus.PAID && paymentStatus != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BusinessException("Cannot complete: booking payment status is " + paymentStatus
                    + ". Full payment is required before completing.");
        }

        book.setBookStatus(BookStatus.COMPLETED);
        List<BookItemEntity> bookingItems = book.getBookItems() != null ? book.getBookItems() : List.of();
        for (BookItemEntity item : bookingItems) {
            RoomEntity room = item.getRoom();
            if (room != null && (room.getStatusRoom() == StatusRoom.ALREADY_CLEAN)) {
                room.setStatusRoom(StatusRoom.AVAILABLE);
                roomRepository.save(room);
            }else{
                throw new RuntimeException("Room is not cleaned yet");
            }

            
        }
        BookEntity saved = bookRepository.save(book);

        Optional<FcmTokenEntity> fcmTokenEntity = fcmTokenRepository
                .findFirstByCustomer_IdAndIsActiveTrueOrderByIdDesc(book.getCustomer().getId());
        {
            String roomNumbers = bookingItems.stream()
                    .map(BookItemEntity::getRoom)
                    .filter(Objects::nonNull)
                    .map(RoomEntity::getRoomNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
            notificationService.bookingCompleted(book.getCustomer().getId(), book.getId(), roomNumbers,
                    fcmTokenEntity.map(FcmTokenEntity::getToken).orElse(null));
        }

        return ApiResponse.success(BookResponse.fromEntity(saved));
    }

    // cancel only book item, not the whole booking
    @Override
    public ApiResponse<Map<String, Object>> cancel_book_item(long item_id) {
        BookItemEntity item = bookItemRepository.findById(item_id)
                .orElseThrow(() -> new NotFoundException("Book item not found with id: " + item_id));
        if (item.getDeletedDate() != null) {
            throw new AlreadyExistException("Book item is already cancelled");
        }
        BookEntity book = item.getBook();
        item.setDeletedDate(LocalDateTime.now());
        item.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        bookItemRepository.save(item);
        if (item.getRoom() != null) {
            item.getRoom().setStatusRoom(StatusRoom.AVAILABLE);
            roomRepository.save(item.getRoom());
        }
        if (book.getTotalAmount() != null) {
            book.setTotalAmount(book.getTotalAmount().subtract(item.getTotalAmount()));
        }
        book.setTotalBookingItems(Math.max(0, book.getTotalBookingItems() - 1));
        boolean noActiveItems = book.getBookItems().stream().allMatch(bookItem ->
                bookItem.getDeletedDate() != null || Objects.equals(bookItem.getId(), item.getId()));
        if (noActiveItems) {
            book.setBookStatus(BookStatus.CANCELLED);
        }
        bookRepository.save(book);

        return ApiResponse.success(Map.of("message", "Book item has been cancelled successfully"));
    }

    // cancel the whole booking, including all book items
    @Override
    public ApiResponse<Map<String, Object>> cancel_booking(long book_id) {
        BookEntity book = bookRepository.findById(book_id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + book_id));
        if (book.getBookStatus() == BookStatus.CANCELLED) {
            throw new AlreadyExistException("Booking is already cancelled");
        }
        book.setBookStatus(BookStatus.CANCELLED);
        bookRepository.save(book);

        List<BookItemEntity> items = book.getBookItems();
        for (BookItemEntity item : items) {
            if (item.getRoom() != null) {
                item.getRoom().setStatusRoom(StatusRoom.AVAILABLE);
                roomRepository.save(item.getRoom());
            }
        }
        return ApiResponse.success(Map.of("message", "Booking has been cancelled successfully"));
    }

    private BookEntity getActiveBooking(Long bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookId));
        if (book.getDeletedDate() != null) {
            throw new NotFoundException("Booking has been deleted");
        }
        return book;
    }

    private BookItemEntity getActiveBookItem(Long bookItemId) {
        BookItemEntity item = bookItemRepository.findById(bookItemId)
                .orElseThrow(() -> new NotFoundException("Book item not found with id: " + bookItemId));
        if (item.getDeletedDate() != null) {
            throw new NotFoundException("Book item has been deleted");
        }
        return item;
    }

    private void validateCustomer(Long customerId) {
        if (customerRepository.existsByIdAndDeletedDateIsNotNull(customerId)) {
            throw new NotFoundException("Customer is deleted");
        }
        if (customerRepository.existsById(customerId)) {
            if (!customerRepository.existsByIdAndStatus(customerId, Status.ACTIVE)) {
                throw new NotFoundException("Customer is not available");
            }
        } else {
            throw new NotFoundException("Customer not found");
        }

    }

    private String generateBookingCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = "BOOKING" + datePart;

        String lastCode = bookRepository.findLastCodeByPrefix(prefix);

        int nextSeq = 1;
        if (lastCode != null) {
            int lastSeq = Integer.parseInt(lastCode.substring(prefix.length()));
            // ^^^^^^^^^^^^^^^^^^^^^^^^
            // "BOOKING26041900003".substring(13) = "00003"
            // Integer.parseInt("00003") = 3

            nextSeq = lastSeq + 1;
            // nextSeq = 4
        }

        return prefix + String.format("%05d", nextSeq);
        // "%05d" = pad with zeros to 5 digits
        // String.format("%05d", 4) = "00004"

        // result = "BOOKING26041900004"
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BookListReponse> getAll() {
        List<BookEntity> books = this.bookRepository.findAll();
        try {
            return ApiResponse.success(bookListReponse(books));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all booking: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BookResponse> getByCustomer(Long Cus_id) {
        customerRepository.findById(Cus_id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + Cus_id));
        try {
            BookEntity book = bookRepository.findByCustomerId(Cus_id).stream()
                    .filter(b -> b.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID
                            || b.getPaymentStatus() == PaymentStatus.UNPAID)
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException(
                            "No partially paid booking found for customer with id: " + Cus_id));
            return ApiResponse.success(BookResponse.fromEntity(book));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get booking by customer id: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<BookResponse> get_byId(Long id) {
        if (this.bookRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new NotFoundException("Booking has been deleted");
        }

        BookEntity book = this.bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
        try {
            return ApiResponse.success(BookResponse.fromEntity(book));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get booking by id: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<BookResponse> get_history_book_cus(Long cus_id) {
        if (cus_id == null) {
            throw new NotFoundException("Customer id not found");
        }
        Optional<BookEntity> get_history = bookRepository.findByCustomerIdAndBookStatus(cus_id, BookStatus.COMPLETED);

        return ApiResponse.success(BookResponse.fromEntity(get_history.get()));

    }

    // get bookings of own customer, customer id taken from the auth principal
    @Transactional(readOnly = true)
    @Override
    public ApiResponse<List<BookResponse>> get_byCustomer(String customerUsername) {
        CustomerEntity customer = this.customerRepository.findByEmail(customerUsername)
                .orElseThrow(() -> new NotFoundException("Customer not found with username: " + customerUsername));
        List<BookEntity> books = this.bookRepository.findByCustomerId(customer.getId());
        try {
            return ApiResponse.success(books.stream().map(BookResponse::fromEntity).collect(Collectors.toList()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get bookings of customer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> delete_byId(Long id) {
        if (this.bookRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new AlreadyExistException("Booking has been deleted");
        }

        BookEntity book = this.bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            book.setDeletedBy(username);
            book.setDeletedDate(LocalDateTime.now());
            book.setBookStatus(BookStatus.CANCELLED);
            this.bookRepository.save(book);

            List<BookItemEntity> bookItems = book.getBookItems();
            for (BookItemEntity item : bookItems) {
                item.setDeletedBy(username);
                if (item.getRoom() != null) {
                    item.getRoom().setStatusRoom(StatusRoom.AVAILABLE);
                }
                item.setDeletedDate(LocalDateTime.now());
                this.bookItemRepository.save(item);
            }

            return ApiResponse.success("Booking deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete booking by id: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<BookListReponse> search_filter(Map<String, String> allParams, UpdateBookRequest filter,
            String sort, int page, int size, boolean isPage, String search) {
        List<BookGroupBy> groupByList = new ArrayList<>();
        try {
            allParams.forEach((key, value) -> {
                try {
                    String enumkey = key;
                    if (key.contains(".")) {
                        String[] parts = key.split("\\.");
                        enumkey = parts[parts.length - 1];

                    }

                    BookGroupBy groupBy = BookGroupBy.valueOf(enumkey.toUpperCase());
                    groupByList.add(groupBy);
                    groupBy.set(filter, value);
                } catch (Exception e) {
                    // TODO: handle exception
                    try {
                        switch (key.toLowerCase()) {
                            case "first_name":
                                filter.getUpdateCustomerRequest().setFirstName(value);
                                break;
                            case "last_name":
                                filter.getUpdateCustomerRequest().setLastName(value);
                                break;
                            case "code":
                                filter.setCode(value);
                                break;
                            case "createdate":
                            case "createddated":
                            case "created_date":
                                filter.setCreateDated(value);
                                break;
                        }
                    } catch (Exception ex) {
                        throw e;
                    }
                }
            });
            List<Sort.Order> sortByList = new ArrayList<>();
            for (String s : sort.split(",")) {
                String[] str = s.split(":");

                if (str.length != 2) {
                    throw new RuntimeException("Invalid sort format like: id:desc");
                }

                String direction = str[1].toUpperCase();
                String field = str[0];

                sortByList
                        .add(new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, field));
            }

            Pageable pageable;
            if (isPage) {
                pageable = PageRequest.of(page - 1, size, Sort.by(sortByList));
            } else {
                // How to set asc and desc in pageable
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sortByList));
            }

            Specification<BookEntity> spec = (root, query, criteriaBuilder) -> {
                // Add DISTINCT to avoid duplicate results
                query.distinct(true);

                if (search == null || search.isEmpty()) {
                    List<Predicate> predicates = new ArrayList<>();

                    if (filter.getPayment_status() != null && !filter.getPayment_status().isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get("paymentStatus"),
                                PaymentStatus.valueOf(filter.getPayment_status().toUpperCase().trim())));
                    }

                    if (filter.getCode() != null && !filter.getCode().isEmpty()) {
                        predicates.add(criteriaBuilder.like(root.get("code"), "%" + filter.getCode() + "%"));
                    }

                    if (filter.getUpdateCustomerRequest().getFirstName() != null) {
                        predicates.add(criteriaBuilder.like(root.get("customer").get("first_name"),
                                "%" + filter.getUpdateCustomerRequest().getFirstName() + "%"));
                    }

                    if (filter.getUpdateCustomerRequest().getLastName() != null) {
                        predicates.add(criteriaBuilder.equal(root.get("customer").get("last_name"),
                                "%" + filter.getUpdateCustomerRequest().getLastName() + "%"));
                    }

                    if (filter.getUpdateBookItemsRequest().getStatus() != null) {
                        predicates.add(criteriaBuilder.equal(root.get("bookStatus"),
                                BookStatus.valueOf(filter.getUpdateBookItemsRequest().getStatus())));
                    }

                    if (filter.getCreateDated() != null && !filter.getCreateDated().isBlank()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate createdDate = LocalDate.parse(filter.getCreateDated().trim(), dateTimeFormatter);
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"),
                                createdDate.atStartOfDay()));
                        predicates.add(criteriaBuilder.lessThan(root.get("createdDate"),
                                createdDate.plusDays(1).atStartOfDay()));
                    }
                    return predicates.isEmpty() ? criteriaBuilder.conjunction()
                            : criteriaBuilder.and(predicates.toArray(new Predicate[0]));

                } else {
                    List<Predicate> allPredicates = new ArrayList<>();

                    // Search predicates (OR condition)
                    List<Predicate> searchPredicates = new ArrayList<>();
                    String request_search = "%" + search.toUpperCase() + "%";
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("code")), request_search));
                    searchPredicates.add(criteriaBuilder
                            .like(criteriaBuilder.upper(root.get("customer").get("firstName")), request_search));
                    searchPredicates.add(criteriaBuilder
                            .like(criteriaBuilder.upper(root.get("customer").get("lastName")), request_search));

                    // Add search OR condition
                    allPredicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));

                    // Filter predicates (AND condition)
                    if (filter.getPayment_status() != null && !filter.getPayment_status().isEmpty()) {
                        allPredicates.add(criteriaBuilder.equal(root.get("payment_status"),
                                PaymentStatus.valueOf(filter.getPayment_status().toUpperCase().trim())));
                    }

                    if (filter.getUpdateBookItemsRequest().getStatus() != null) {
                        allPredicates.add(criteriaBuilder.equal(root.get("bookStatus"),
                                BookStatus.valueOf(filter.getUpdateBookItemsRequest().getStatus())));
                    }

                    if (filter.getCreateDated() != null && !filter.getCreateDated().isBlank()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate createdDate = LocalDate.parse(filter.getCreateDated().trim(), dateTimeFormatter);
                        allPredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"),
                                createdDate.atStartOfDay()));
                        allPredicates.add(criteriaBuilder.lessThan(root.get("createdDate"),
                                createdDate.plusDays(1).atStartOfDay()));
                    }

                    // Combine all with AND
                    return criteriaBuilder.and(allPredicates.toArray(new Predicate[0]));
                }
            };

            return ApiResponse.success(bookListReponse(bookRepository.findAll(spec, pageable).getContent()));

        } catch (Exception e) {
            throw e;
        }
    }

    private BookListReponse bookListReponse(List<BookEntity> bookEntities) {
        int totalBooking = (int) bookRepository.count();
        int totalCancel = bookRepository.countByDeletedDateIsNotNull();
        int totalpaid = bookRepository.countByPaymentStatusAndDeletedDateIsNull(PaymentStatus.PAID);
        int totalPartially_paid = bookRepository.countByPaymentStatusAndDeletedDateIsNull(PaymentStatus.PARTIALLY_PAID);
        int totalUnpaid = bookRepository.countByPaymentStatusAndDeletedDateIsNull(PaymentStatus.UNPAID);
        int totalPartially_refund = bookRepository
                .countByPaymentStatusAndDeletedDateIsNull(PaymentStatus.PARTIALLY_REFUNDED);
        int totalRefund = bookRepository.countByPaymentStatusAndDeletedDateIsNull(PaymentStatus.REFUNDED);

        return BookListReponse.builder()
                .totalBooking(totalBooking)
                .totalCancel(totalCancel)
                .totalPaid(totalpaid)
                .totalPartially_paid(totalPartially_paid)
                .totalUnpaid(totalUnpaid)
                .totalPartially_refund(totalPartially_refund)
                .totalRefund(totalRefund)
                .bookResponses(bookEntities.stream().map(BookResponse::fromEntity).toList())
                .build();
    }

}
