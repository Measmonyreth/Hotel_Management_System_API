package com.veth007.hotelmanagement.repository;

import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.model.entity.Customer_notificationEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;
import com.veth007.hotelmanagement.constant.enums.NotificationType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veth007.hotelmanagement.constant.enums.NotificationScope;

@Repository
public interface Customer_notificationRepository extends JpaRepository<Customer_notificationEntity, Long> {

        // JOIN FETCH avoids N+1 when you read notification fields
        @Query("""
                        select distinct n from Customer_notificationEntity cn
                        join cn.notification n
                        where cn.customer.id = :customerId
                          and n.scope = :scope
                        """)
        List<NotificationEntity> findNotificationsByCustomerIdAndScope(
                        @Param("customerId") Long customerId,
                        @Param("scope") NotificationScope scope);

        @Modifying
        @Query("update Customer_notificationEntity n set n.readAt = :now " +
                        "where n.notification.id = :notificationId and n.customer.id = :customerId and n.readAt is null")
        int updatemarkRead(@Param("notificationId") Long notificationId, @Param("customerId") Long customerId,
                        @Param("now") Instant now);

        @Modifying
        @Query(value = """
                        insert into customer_notification (customer_id, notification_id, read_at)
                        select :customerId, :notificationId, :now
                        where not exists (
                            select 1
                            from customer_notification
                            where customer_id = :customerId
                              and notification_id = :notificationId
                        )
                        """, nativeQuery = true)
        int insertRead(@Param("notificationId") Long notificationId,
                        @Param("customerId") Long customerId,
                        @Param("now") Instant now);

        boolean existsByNotification_IdAndCustomer_Id(Long notificationId, Long customerId);

        Optional<Customer_notificationEntity> findFirstByNotification_TypeAndNotification_EntityIdAndCustomer_Id(
                        NotificationType type, Long bookingId, Long customerId);

        boolean existsByNotification_IdAndCustomer_IdAndReadAtIsNotNull(Long notificationId, Long customerId);

        // List<Customer_notificationEntity> findAllWithNotification();
        long countByNotification_Scope(NotificationScope scope);

        long countByCustomer_IdAndReadAtIsNotNullAndNotification_Scope(Long customerId,
                        NotificationScope scope);

        long countByCustomer_IdAndReadAtIsNullAndNotification_Scope(Long customerId,
                        NotificationScope scope);

}
