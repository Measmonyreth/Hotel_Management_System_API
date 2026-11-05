package com.veth007.hotelmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.model.entity.DiscountEntity;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountEntity, Long>, JpaSpecificationExecutor<DiscountEntity>{
    @Query(value = "SELECT code FROM discount WHERE code LIKE CONCAT(:prefix, '%') ORDER BY code DESC LIMIT 1", nativeQuery = true)
    String findLastCodeByPrefix(@Param("prefix") String prefix);
}
