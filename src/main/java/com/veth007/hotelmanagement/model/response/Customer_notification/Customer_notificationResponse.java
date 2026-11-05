package com.veth007.hotelmanagement.model.response.Customer_notification;

import java.io.Serializable;

import com.google.api.services.storage.model.Notification;
import com.veth007.hotelmanagement.model.entity.Customer_notificationEntity;
import com.veth007.hotelmanagement.model.response.notification.NotificationResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Customer_notificationResponse implements Serializable {
    private long id;
    private Object customer;
    private String read_at;
    private NotificationResponse notificationResponse;

    static public Customer_notificationResponse fromEntity(Customer_notificationEntity entity) {
        return Customer_notificationResponse.builder()
                .id(entity.getId())
                .customer(entity.getCustomer())
                .read_at(String.valueOf(entity.getReadAt()))
                .notificationResponse(NotificationResponse.fromEntity(entity.getNotification()))
                .build();
    }
}
