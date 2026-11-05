package com.veth007.hotelmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationImageEntity;

@Repository 
public interface  NotificationImageRepository extends JpaRepository<NotificationImageEntity, Long> {
    List<NotificationImageEntity> findByNotificationId(Long noti_Id);
}