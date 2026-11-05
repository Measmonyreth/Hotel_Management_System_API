package com.veth007.hotelmanagement.model.request.notification;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationImageEntity.TypeImage;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest implements Serializable {

    // booking-success notification
    private Long customerId;
    private Long bookingId;
    private PaymentStatus paidStatus;
    @NotNull
    private Integer amountPaid;
    @NotNull
    private Integer amountDue;

    // new-promotion notification
    @JsonAlias({ "token", "fcmToken" })
    private String recipientToken;
    private String title;
    private String body;
    // private String image;
    // private String icon;
    private String notification_type;
    private String notification_scope;
}
