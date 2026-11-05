package com.veth007.hotelmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.model.entity.CancelRequestEntity;

@Repository
public interface CancelRepository extends JpaRepository<CancelRequestEntity, Long> {
    CancelRequestEntity findByBookId(Long b_id);
    boolean existsByBookId(Long book_id);
    List<CancelRequestEntity> findByBook_Customer_IdAndDeletedDateIsNull(Long customerId);
}
