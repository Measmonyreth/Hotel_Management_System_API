package com.veth007.hotelmanagement.repository;

import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.model.entity.BookItemEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookItemRepository extends JpaRepository<BookItemEntity, Long>, JpaSpecificationExecutor<BookItemEntity> {
    boolean existsByBookId(Long id);

    Optional<BookItemEntity> findFirstByBookIdOrderByIdAsc(Long bookId);
   // Optional<BookItemEntity> findFirstByBookIdAndRemainingAmountIsNotNullOrderByIdAsc(Long bookId);
    List<BookItemEntity> findByBookId(Long bookId);

    @Query("SELECT COALESCE(SUM(b.discountAmount), 0) FROM bookItem b WHERE b.book.id = :bookId")
    BigDecimal sumDiscountAmountByBookId(@Param("bookId") Long bookId);

    // only paid bookings hold a room: unpaid ones are leftovers of the
    // pay-first flow (payment never completed) and do not block the dates
//     @Query("SELECT COUNT(b) > 0 FROM bookItem b " +
//             "WHERE b.room.id = :roomId AND b.deletedDate IS NULL " +
//             "AND b.status NOT IN :excludedStatuses " +
//             "AND b.book.paymentStatus IN :activePaymentStatuses " +
//             "AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
//     boolean existsOverlappingBooking(@Param("roomId") Long roomId,
//             @Param("checkIn") LocalDateTime checkIn,
//             @Param("checkOut") LocalDateTime checkOut,
//             @Param("excludedStatuses") List<BookStatus> excludedStatuses,
//             @Param("activePaymentStatuses") List<PaymentStatus> activePaymentStatuses);
}
