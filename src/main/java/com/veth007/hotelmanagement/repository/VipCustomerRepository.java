package com.veth007.hotelmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.constant.enums.Type_vip;
import com.veth007.hotelmanagement.constant.enums.VipStatus;
import com.veth007.hotelmanagement.model.entity.VipCustomerEntity;

@Repository
public interface VipCustomerRepository
        extends JpaRepository<VipCustomerEntity, Long>, JpaSpecificationExecutor<VipCustomerEntity> {
    Optional<VipCustomerEntity> findByCustomerId(Long id);

    boolean existsByStatus(VipStatus status);

    Optional<VipCustomerEntity> findByIdAndDeletedDateIsNull(Long id);

    Optional<VipCustomerEntity> findByIdAndDeletedDateIsNotNull(Long id);

    List<VipCustomerEntity> findByStatus(VipStatus status);

    boolean existsByIdAndDeletedDateIsNotNull(Long id);

    boolean existsByIdAndDeletedDateIsNull(Long id);

    boolean existsByIdAndStatus(Long id, VipStatus status);

    @Query(value = "SELECT code FROM vip_customers WHERE code LIKE CONCAT(:prefix, '%') ORDER BY code DESC LIMIT 1", nativeQuery = true)
    String findLastCodeByPrefix(@Param("prefix") String prefix);

    boolean existsByCustomerIdAndStatus(Long id, VipStatus status);

    int countByStatus(VipStatus status);
    int countByDeletedDateIsNotNull();
int countByTypeVipAndDeletedDateIsNullAndStatusIsNot(Type_vip typeVip, VipStatus status);

}
