package com.veth007.hotelmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.veth007.hotelmanagement.constant.enums.Shift;
import com.veth007.hotelmanagement.model.entity.ShiftEntity;
import java.time.LocalTime;


@Repository
public interface ShiftRepository extends JpaRepository<ShiftEntity, Long>, JpaSpecificationExecutor<ShiftEntity> {
    List<ShiftEntity> findByShift(Shift shift);
    boolean existsByShift(Shift shift);
    List<ShiftEntity> findByDayOff(String dayOff);
    List<ShiftEntity> findByDayWork(String dayWork);
    List<ShiftEntity> findByDayOffAndDayWork(String dayOff, String dayWork);
    List<ShiftEntity> findByStartingTime(LocalTime startingTime);
    List<ShiftEntity> findByEndingTime(LocalTime endingTime);
    List<ShiftEntity> findByStartingTimeAndEndingTime(LocalTime startingTime, LocalTime endingTime);

    @Query("SELECT s FROM ShiftEntity s ORDER BY s.id ASC")
    List<ShiftEntity> findAllSorted();

    boolean existsByIdAndDeletedDateIsNotNull(Long id);
    boolean existsByIdAndDeletedByIsNotNull(Long id);
    boolean existsByDeletedDateIsNotNull();
    boolean existsByDeletedByIsNotNull();
    List<ShiftEntity> findByDeletedByIsNotNull();
    List<ShiftEntity> findByDeletedDateIsNotNull();
    ShiftEntity findByIdAndDeletedByIsNotNull(Long id);

    int countByShift(Shift shift);
    int countByDayWork(String dayWork);
    int countByStartingTimeAndEndingTime(LocalTime startingTime, LocalTime endingTime);
}
    
