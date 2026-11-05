package com.veth007.hotelmanagement.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veth007.hotelmanagement.constant.enums.NotificationScope;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
        // personal feed for one customer (uses idx_notif_customer)
        // @EntityGraph(attributePaths = "customer")
        // List<NotificationEntity> findByScopeAndCustomer_IdOrderByCreatedDateDesc(
        //                 NotificationScope scope, Long customerId);

        // @EntityGraph(attributePaths = "customer")
        // @Query("select n from NotificationEntity n " +
        //                 "where (n.scope = :personalScope and n.customer.id = :customerId) " +
        //                 "or n.scope = :generalScope " +
        //                 "order by n.createdDate desc")
        // List<NotificationEntity> findPersonalAndGeneralForCustomer(
        //                 @Param("personalScope") NotificationScope personalScope,
        //                 @Param("generalScope") NotificationScope generalScope,
        //                 @Param("customerId") Long customerId);

        // // unread badge count (uses idx_notif_customer_unread)
        // long countByScopeAndCustomer_IdAndReadAtIsNull(
        //                 NotificationScope scope, Long customerId);

        // general broadcasts
        List<NotificationEntity> findByScopeOrderByCreatedDateDesc(NotificationScope scope);

        @Query(value = "select n from NotificationEntity n order by n.createdDate desc",
                        countQuery = "select count(n) from NotificationEntity n")
        Page<NotificationEntity> findAllWithCustomerNotificationAndNotificationImage(Pageable pageable);

        @Query("select n from NotificationEntity n left join fetch n.customerNotifications where n.id = :id")
        List<NotificationEntity> findByIdWithCustomerNotificationAndNotificationImage(@Param("id") Long id);

        // Fetch this collection separately: fetching both List associations in one
        // query can trigger Hibernate's MultipleBagFetchException.
        @Query("select n from NotificationEntity n left join fetch n.image where n.id = :id")
        List<NotificationEntity> findByIdWithNotificationImages(@Param("id") Long id);

        // @Modifying
        // @Query("update NotificationEntity n set n.readAt = :now " +
        //                 "where n.id = :id and n.customer.id = :customerId and n.readAt is null")
        // int markRead(@Param("id") Long id, @Param("customerId") Long customerId, @Param("now") Instant now);

        // @Modifying
        // @Query("update NotificationEntity n set n.readAt = :now " +
        //                 "where n.customer.id = :customerId and n.readAt is null")
        // int markReadAll(@Param("customerId") Long customerId, @Param("now") Instant now);

        int countByScope(NotificationScope notificationScope);
        // int countByReadAtIsNotNull();
        // int countByReadAtIsNull();



        List<NotificationEntity> findByScopeOrderByCreatedDate(NotificationScope scope);
}
