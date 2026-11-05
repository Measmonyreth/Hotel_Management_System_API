package com.veth007.hotelmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.model.entity.BookEntity;


@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long>, JpaSpecificationExecutor<BookEntity>{

    // find by id customer
    List<BookEntity> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {
            "customer",
            "employee",
            "bookItems",
    })
    @Override
    Optional<BookEntity> findById(Long id);

    @EntityGraph(attributePaths = {
            "customer",
            "employee",
            "bookItems",
            "bookItems.room"
    })
    @Query("SELECT DISTINCT b FROM books b")
    @Override
    List<BookEntity> findAll();

    BookEntity findFirstByCustomerIdAndDeletedDateIsNullOrderByCreatedDateDesc(Long customerId);


    @Query(value = "SELECT code FROM books WHERE code LIKE CONCAT(:prefix, '%') ORDER BY code DESC LIMIT 1", nativeQuery = true)
    String findLastCodeByPrefix(@Param("prefix") String prefix);

    boolean existsByIdAndDeletedDateIsNotNull(Long id);

   Optional<BookEntity> findByIdAndCustomerId(Long id, Long customerId);

   Optional<BookEntity> findByCustomerIdAndBookStatus(Long id, BookStatus bookStatus);

   int countByPaymentStatusAndDeletedDateIsNull(PaymentStatus paymentStatus);

   // for cancel is equal deleted
   int countByDeletedDateIsNotNull();
}
