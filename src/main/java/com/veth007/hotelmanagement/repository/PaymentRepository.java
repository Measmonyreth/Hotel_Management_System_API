package com.veth007.hotelmanagement.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.model.entity.PaymentEntity;
import com.veth007.hotelmanagement.model.response.PaymentResponse.CurrencyTotal;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long>, JpaSpecificationExecutor<PaymentEntity> {
  // Optional<PaymentEntity> findByBookIdAndPaymentStatus(Long bookId,
  // PaymentStatus paymentStatus);
 PaymentEntity findByBookId(Long bookId);

 @Query("""
     SELECT DISTINCT p
     FROM payment p
     JOIN FETCH p.book b
     LEFT JOIN FETCH b.bookItems
     WHERE p.id = :paymentId
       AND p.deletedDate IS NULL
     """)
 PaymentEntity findActiveByIdWithBookItems(@Param("paymentId") Long paymentId);

 List<PaymentEntity> findAllByBookIdAndDeletedDateIsNull(Long bookId);

  Optional<PaymentEntity> findByIdAndDeletedDateIsNotNull(Long id);

  boolean existsByIdAndDeletedDateIsNull(Long id);

  @Query("""
      SELECT p.currency AS currency,
             SUM(p.paidAmount - COALESCE(p.refundedAmount, 0)) AS total
      FROM payment p
      WHERE p.deletedDate IS NULL
        AND p.paymentStatus IN :statuses
      GROUP BY p.currency
      """)
  List<CurrencyTotal> sumNetByCurrency(@Param("statuses") List<PaymentStatus> statuses);

  
  @Query("""
      SELECT COALESCE(SUM(p.refundedAmount), 0)
      FROM payment p
      WHERE p.deletedDate IS NULL
        AND p.paymentStatus IN :statuses
      """)
  BigDecimal sumRefundedAmount(@Param("statuses") List<PaymentStatus> statuses);

  @Query("SELECT COUNT(p) FROM payment p WHERE p.paymentStatus = :paymentStatus AND p.deletedDate IS NULL")
  long countActiveByPaymentStatus(@Param("paymentStatus") PaymentStatus paymentStatus);

  int countByPaymentStatus(PaymentStatus paymentStatus);


  List<PaymentEntity> findByPaymentStatusInAndDeletedDateIsNull(List<PaymentStatus> statuses);
}