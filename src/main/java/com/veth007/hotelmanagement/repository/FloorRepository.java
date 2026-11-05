package com.veth007.hotelmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.veth007.hotelmanagement.model.entity.FloorEntity;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import com.veth007.hotelmanagement.constant.enums.StatusFloor;


@Repository
public interface FloorRepository extends JpaRepository<FloorEntity, Long>, JpaSpecificationExecutor<FloorEntity>{
    boolean existsByFloorNumber(String floorNumber);
    boolean existsByFloorName(String floorName);
    boolean existsByFloorNameIgnoreCase(String floorName);
    Optional<FloorEntity> findByFloorNameIgnoreCase(String floorName);
    Optional<FloorEntity> findByFloorNumber(String floorNumber);
    boolean existsByDeletedDateIsNotNull();
    boolean existsByDeletedByIsNotNull();
    boolean existsByIdAndDeletedByIsNull(Long id);
//    boolean existsByActionEqualsDeleted();

    List<FloorEntity> findByDeletedByIsNotNull();
    List<FloorEntity> findByDeletedDateIsNotNull();

        FloorEntity findByIdAndDeletedByIsNotNull(Long id);

    boolean existsByIdAndDeletedByIsNotNull(Long id);

    long countByStatusFloor(StatusFloor statusFloor);

}
