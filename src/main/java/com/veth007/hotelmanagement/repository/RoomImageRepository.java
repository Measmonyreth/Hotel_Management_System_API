package com.veth007.hotelmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.veth007.hotelmanagement.model.entity.Room.RoomImageEntity;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImageEntity, Long> {
    List<RoomImageEntity> findByRoomId(Long roomId);
}
