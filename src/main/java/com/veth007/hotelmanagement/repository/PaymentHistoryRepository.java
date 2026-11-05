package com.veth007.hotelmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.model.entity.PaymentHistory;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long>, JpaSpecificationExecutor<PaymentHistory> {
    PaymentHistory findByPaymentId(Long paymentId);
    List<PaymentHistory> findAllByPaymentId(Long paymentId);
}
