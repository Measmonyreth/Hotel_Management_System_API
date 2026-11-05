package com.veth007.hotelmanagement.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.veth007.hotelmanagement.constant.enums.RoomType;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.StatusRoom;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;

@Repository
public interface RoomRepository
        extends JpaRepository<RoomEntity, Long>, JpaSpecificationExecutor<RoomEntity> {
    Optional<RoomEntity> findByIdAndDeletedDateIsNull(Long id);


    Optional<RoomEntity> findByIdAndDeletedDateIsNotNull(Long id);
    Optional<RoomEntity> findByRoomNameIgnoreCase(String roomName);
    Optional<RoomEntity> findByRoomNumber(String roomNumber);
    Optional<RoomEntity> findByRoomNumberAndStatusRoom(String roomNumber, StatusRoom statusRoom);

    boolean existsByRoomName(String roomName);
    boolean existsByRoomNameIgnoreCase(String roomName);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByIdAndDeletedDateIsNotNull(Long id);

    boolean existsByDeletedByIsNotNull();

    List<RoomEntity> findByDeletedByIsNotNull();

    boolean existsByDeletedDateIsNotNull();

    boolean existsByIdAndStatusRoom(Long id, StatusRoom statusRoom);

    boolean existsByIdAndAction(Long id, Status status);
    boolean existsByIdAndDeletedByIsNull(Long id);

    @Query("SELECT COUNT(r) FROM rooms r WHERE r.floor.id IN :floorIds")
    long countByFloorIds(@Param("floorIds") List<Long> floorIds);

    @Query("SELECT COUNT(r) FROM rooms r WHERE r.floor.id IN :floorIds AND r.statusRoom = :statusRoom")
    long countByFloorIdsAndStatusRoom(@Param("floorIds") List<Long> floorIds,
            @Param("statusRoom") StatusRoom statusRoom);

    @Query("SELECT COUNT(r) FROM rooms r WHERE r.floor.id = :floorId")
    long countByFloorId(@Param("floorId") Long floorId);

    @Query("SELECT COUNT(r) FROM rooms r WHERE r.floor.id = :floorId AND r.statusRoom = :statusRoom")
    long countByFloorIdAndStatusRoom(@Param("floorId") Long floorId, @Param("statusRoom") StatusRoom statusRoom);

    int countByStatusRoom(StatusRoom statusRoom);
    int countByAction(Status status);
    int countByType(RoomType roomType);
    //int count();
}
