package com.veth007.hotelmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.veth007.hotelmanagement.model.entity.SearchHistoryEntity;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntity, Long> {

    // personal search history for one customer (most recent first)
    List<SearchHistoryEntity> findByCustomer_IdOrderByCreatedDateDesc(Long customerId);

    long countByCustomer_Id(Long customerId);

    void deleteByCustomer_Id(Long customerId);
}
