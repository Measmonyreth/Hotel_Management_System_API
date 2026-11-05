package com.veth007.hotelmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.veth007.hotelmanagement.constant.enums.Shift;
import com.veth007.hotelmanagement.model.entity.ShiftEntity;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<ShiftEntity, Long> {
        List<ShiftEntity> findByShift(Shift shift);

}
