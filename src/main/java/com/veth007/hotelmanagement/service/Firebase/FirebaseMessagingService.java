package com.veth007.hotelmanagement.service.Firebase;

import org.springframework.web.multipart.MultipartFile;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.notification.NotificationRequest;

public interface FirebaseMessagingService {
    ApiResponse<String> sendNotificationByToken(NotificationRequest notificationRequest,MultipartFile image, MultipartFile icon);
    ApiResponse<String> sendPushByToken(NotificationRequest notificationRequest);
    ApiResponse<String> sendNotificationByTopic(NotificationRequest notificationRequest, String topic,MultipartFile image, MultipartFile icon);
    //ApiResponse<String> sendPromotionNotification(NotificationRequest notificationRequest);
}
