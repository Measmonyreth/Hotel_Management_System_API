package com.veth007.hotelmanagement.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.veth007.hotelmanagement.constant.enums.TaskStatus;
import com.veth007.hotelmanagement.model.entity.TaskEnttity;

@Repository
public interface TaskRepository extends JpaRepository<TaskEnttity, Long>, JpaSpecificationExecutor<TaskEnttity> {
    boolean existsByIdAndDeletedDateIsNotNull(Long id);
    List<TaskEnttity> findByEmployeeId(Long employeeId);
   TaskEnttity findByRoomId(Long roomId);

    TaskEnttity findByIdAndDeletedDateIsNotNull(Long id);
    boolean existsByDeletedDateIsNotNull();
    boolean existsByDeletedByIsNotNull();
    List<TaskEnttity> findByDeletedByIsNotNull();
    List<TaskEnttity> findByDeletedDateIsNotNull();
    List<TaskEnttity> findByDeletedByIsNotNullAndDeletedDateIsNotNull();
    int countByTaskStatus(TaskStatus taskStatus);
}
