package com.veth007.hotelmanagement.model.response.notification;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse implements  Serializable{
    private Long id;
    private String scope;
    private String type;
    private String title;
    private String body;
    private String entityType; // "booking", "promotion", "room", or null
    private Long entityId; // the promotion / booking / room id (depends on entityType)
    private String paidStatus; // only for booking_success
    private String actionUrl;
    private Map<String, Object> data;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;
    private List<NotificationImageResponse> notificationImageResponses;
    private List<Customer_notificationResponse> customer_notificationResponse;

    public static NotificationResponse fromEntity(NotificationEntity entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());
        response.setScope(entity.getScope() != null ? entity.getScope().name() : null);
        response.setType(entity.getType() != null ? entity.getType().name() : null);
        response.setTitle(entity.getTitle());
        response.setBody(entity.getBody());
        response.setEntityType(entity.getEntityType());
        response.setEntityId(entity.getEntityId());
        response.setPaidStatus(entity.getPaidStatus() != null ? entity.getPaidStatus().name() : null);
        response.setActionUrl(entity.getActionUrl());
        response.setData(entity.getData());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        response.setDeletedDate(entity.getDeletedDate());
        response.setCreateBy(entity.getCreatedBy());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setDeletedBy(entity.getDeleted_by());
        response.setNotificationImageResponses(
                entity.getImage() != null
                        ? entity.getImage().stream().map(NotificationImageResponse::fromEntity).toList()
                        : null);
        response.setCustomer_notificationResponse(
                entity.getCustomerNotifications() != null
                        ? entity.getCustomerNotifications().stream().map(Customer_notificationResponse::fromEnitty).toList()
                        : null);
        return response;
    }
}
