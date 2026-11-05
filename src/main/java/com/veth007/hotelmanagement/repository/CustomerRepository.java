package com.veth007.hotelmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.CustomerEntity.AuthProvider;

@Repository
public interface CustomerRepository
        extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {
    Optional<CustomerEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<CustomerEntity> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<CustomerEntity> findByRole(Role role);

    List<CustomerEntity> findByStatus(Status status);

    boolean existsByStatus(Status status);

    Optional<CustomerEntity> findByIdAndDeletedDateIsNull(Long id);

    boolean existsByIdAndDeletedDateIsNotNull(Long id);

    boolean existsByIdAndStatus(Long id, Status status);

    Optional<CustomerEntity> findByIdAndDeletedDateIsNotNull(Long id);

    Optional<CustomerEntity> findTopByOrderByCustomerIdDesc();

    // Optional<CustomerEntity> findByIdAndCustomerId(Long id, Long customerId);

    Optional<CustomerEntity> findByFirstNameAndLastName(String firstName, String lastName);

    int countByStatus(Status status);

    int countByProvider(AuthProvider provider);
}
