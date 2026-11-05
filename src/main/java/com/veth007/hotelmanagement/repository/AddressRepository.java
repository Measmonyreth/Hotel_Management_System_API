package com.veth007.hotelmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.model.entity.AddressEntity;

@Repository

public interface AddressRepository extends JpaRepository<AddressEntity, Long>, JpaSpecificationExecutor<AddressEntity> {
    
}
