package com.veth007.hotelmanagement.model.response.notification;

import java.util.List;

import com.veth007.hotelmanagement.constant.enums.NotificationScope;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;
import com.veth007.hotelmanagement.repository.NotificationRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {
    private long totalNotification;
    private long totalUnreadNotification;
    private long totalReadNotification;
    private long totalPersonalNotification;
    private long totalGeneralNotification;
    private List<NotificationResponse> notificationResponses;

    public static NotificationListResponse fromEntity(
            List<NotificationEntity> notificationEntities,
            NotificationRepository notificationRepository) {
        long totalNotification = notificationRepository.count();

        // long totalUnreadNotification = notificationRepository.countByReadAtIsNull();
        // long totalReadNotification = notificationRepository.countByReadAtIsNotNull();
        long totalPersonalNotification = notificationRepository.countByScope(NotificationScope.PERSONAL);
        long totalGeneralNotification = notificationRepository.countByScope(NotificationScope.GENERAL);
        return NotificationListResponse.builder()
                .totalNotification(totalNotification)
                // .totalUnreadNotification(totalUnreadNotification)
                // .totalReadNotification(totalReadNotification)
                .totalPersonalNotification(totalPersonalNotification)
                .totalGeneralNotification(totalGeneralNotification)
                .notificationResponses(notificationEntities.stream().map(NotificationResponse::fromEntity).toList())
                .build();
    }

}
