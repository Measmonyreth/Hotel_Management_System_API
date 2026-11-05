package com.veth007.hotelmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.model.entity.EmployeeEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
                extends JpaRepository<EmployeeEntity, Long>, JpaSpecificationExecutor<EmployeeEntity> {
        Optional<EmployeeEntity> findByUsername(String username);

        Optional<EmployeeEntity> findByPhone(String phone);

        List<EmployeeEntity> findByRole(Role role);

        List<EmployeeEntity> findByStatus(Status status);

        boolean existsByUsername(String username);
        boolean existsByPhone(String phone);

        Optional<EmployeeEntity> findByIdAndDeletedDateIsNull(Long id);

        boolean existsByIdAndDeletedDateIsNotNull(Long id);

        boolean existsByIdAndDeletedDateIsNull(Long id);

        boolean existsByUsernameAndStatus(String username, Status status);

        boolean existsByIdAndStatus(Long id, Status status);

        Optional<EmployeeEntity> findTopByOrderByEmployeeIdDesc();

        @Modifying
        @Transactional
        @Query(value = "UPDATE employee_entity SET status = :status, deleted_date = :deletedDate, delete_by = :deletedBy WHERE id = :id", nativeQuery = true)
        void softDeleteById(
                        @Param("id") Long id,
                        @Param("status") String status,
                        @Param("deletedDate") LocalDateTime deletedDate,
                        @Param("deletedBy") String deletedBy);

        @Modifying
        @Transactional
        @Query(value = "UPDATE employee_entity SET status = :status, deleted_date = null, delete_by = null WHERE id = :id", nativeQuery = true)
        void restoreById(
                        @Param("id") Long id,
                        @Param("status") String status);

        int countByStatus(Status status);
        int countByRole(Role role);

}
