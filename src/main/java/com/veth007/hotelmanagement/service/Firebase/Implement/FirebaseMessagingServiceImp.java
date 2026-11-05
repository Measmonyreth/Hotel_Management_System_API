package com.veth007.hotelmanagement.service.Firebase.Implement;

import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.veth007.hotelmanagement.constant.enums.NotificationScope;
import com.veth007.hotelmanagement.constant.enums.NotificationType;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.Customer_notificationEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationImageEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationImageEntity.TypeImage;
import com.veth007.hotelmanagement.model.request.notification.NotificationRequest;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.Customer_notificationRepository;
import com.veth007.hotelmanagement.repository.FcmTokenRepository;
import com.veth007.hotelmanagement.repository.NotificationRepository;
import com.veth007.hotelmanagement.service.Firebase.FirebaseMessagingService;
import com.veth007.hotelmanagement.service.notification.NotificationImageService;
import com.veth007.hotelmanagement.service.notification.NotificationService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FirebaseMessagingServiceImp implements FirebaseMessagingService {
        private static final Logger log = LoggerFactory.getLogger(FirebaseMessagingServiceImp.class);

        private FirebaseMessaging firebaseMessaging;
        private NotificationRepository notificationRepository;
        private FcmTokenRepository fcmTokenRepository;
        private Customer_notificationRepository customer_notificationRepository;
        private NotificationImageService notifi_img_service;

        @Override
        public ApiResponse<String> sendNotificationByToken(NotificationRequest notificationRequest, MultipartFile file,
                        MultipartFile icon) {
                validateCommonPayload(notificationRequest);
                String recipientToken = requireText(notificationRequest.getRecipientToken(), "recipientToken");
                // get email, full name of custosmer from recipientToken and save to DB
                CustomerEntity customer = fcmTokenRepository.findByToken(recipientToken)
                                .map(token -> token.getCustomer())
                                .orElse(null);
                try {

                        NotificationEntity notificationEntity = notificationRepository.save(NotificationEntity.builder()

                                        .type(notificationRequest.getNotification_type() != null
                                                        ? NotificationType.valueOf(
                                                                        notificationRequest.getNotification_type())
                                                        : NotificationType.INFO_GENERAL)
                                        .scope(notificationRequest.getNotification_scope() != null
                                                        ? NotificationScope.valueOf(
                                                                        notificationRequest.getNotification_scope())
                                                        : NotificationScope.PERSONAL)
                                        .title(notificationRequest.getTitle())
                                        .body(notificationRequest.getBody())
                                        .notificationStatus(Status.ACTIVE)
                                        .build());

                        String imageUrl = null;
                        if (file != null && !file.isEmpty()) {
                                imageUrl = notifi_img_service
                                                .upload_image(notificationEntity.getId(), file, TypeImage.IMAGE)
                                                .getNotification_url();
                        }
                        String iconUrl = null;
                        if (icon != null && !icon.isEmpty()) {
                                iconUrl = notifi_img_service
                                                .upload_image(notificationEntity.getId(), icon, TypeImage.ICON)
                                                .getNotification_url();
                        }

                        Notification.Builder notificationBuilder = Notification.builder()
                                        .setTitle(notificationRequest.getTitle())
                                        .setBody(notificationRequest.getBody());
                        if (imageUrl != null) {
                                notificationBuilder.setImage(imageUrl);
                        }
                        Message.Builder messageBuilder = Message.builder()
                                        .setNotification(notificationBuilder.build())
                                        .setToken(recipientToken);
                        if (iconUrl != null) {
                                messageBuilder.putData("icon", iconUrl);
                        }

                        Customer_notificationEntity customer_notificationEntity = customer_notificationRepository.save(
                                        Customer_notificationEntity.builder()
                                                        .notification(notificationEntity)
                                                        .customer(fcmTokenRepository.findByToken(recipientToken)
                                                                        .map(token -> token.getCustomer())
                                                                        .orElse(null))
                                                        .build());

                        return ApiResponse.success(firebaseMessaging.send(messageBuilder.build()));
                } catch (FirebaseMessagingException e) {
                        log.error("Firebase send-to-token failed. errorCode={}, message={}",
                                        e.getMessagingErrorCode(), e.getMessage(), e);
                        throw new IllegalArgumentException(buildFirebaseErrorMessage("token", e));
                }
        }

        /** Sends a push only. Booking notifications are persisted by NotificationService. */
        @Override
        public ApiResponse<String> sendPushByToken(NotificationRequest notificationRequest) {
                validateCommonPayload(notificationRequest);
                String recipientToken = requireText(notificationRequest.getRecipientToken(), "recipientToken");
                Notification.Builder notification = Notification.builder()
                                .setTitle(notificationRequest.getTitle())
                                .setBody(notificationRequest.getBody());
                try {
                        return ApiResponse.success(firebaseMessaging.send(Message.builder()
                                        .setNotification(notification.build())
                                        .setToken(recipientToken)
                                        .build()));
                } catch (FirebaseMessagingException e) {
                        log.error("Firebase push-only send failed. errorCode={}, message={}",
                                        e.getMessagingErrorCode(), e.getMessage(), e);
                        throw new IllegalArgumentException(buildFirebaseErrorMessage("token", e));
                }
        }

        // general broadcast: send to everyone subscribed to a topic (no per-device
        // token)
        @Override
        public ApiResponse<String> sendNotificationByTopic(NotificationRequest notificationRequest, String topic,
                        MultipartFile file, MultipartFile icon) {
                validateCommonPayload(notificationRequest);
                String normalizedTopic = requireTopic(topic);

                try {

                        NotificationEntity notificationRequestForDB = NotificationEntity.builder()
                                        .type(notificationRequest.getNotification_type() != null
                                                        ? NotificationType.valueOf(
                                                                        notificationRequest.getNotification_type())
                                                        : NotificationType.INFO_GENERAL)
                                        .scope(notificationRequest.getNotification_scope() != null
                                                        ? NotificationScope.valueOf(
                                                                        notificationRequest.getNotification_scope())
                                                        : NotificationScope.GENERAL)
                                        .title(notificationRequest.getTitle())
                                        .body(notificationRequest.getBody())
                                        .notificationStatus(Status.ACTIVE)
                                        // .image(normalizeImage(notificationRequest.getImage()) != null
                                        // ? normalizeImage(notificationRequest.getImage())
                                        // : null)
                                        // .icon(normalizeImage(notificationRequest.getIcon()) != null
                                        // ? normalizeImage(notificationRequest.getIcon())
                                        // : null)
                                        .build();
                        notificationRepository.save(notificationRequestForDB);
                        String imageUrl = null;
                        if (file != null && !file.isEmpty()) {
                                imageUrl = notifi_img_service
                                                .upload_image(notificationRequestForDB.getId(), file, TypeImage.IMAGE)
                                                .getNotification_url();
                        }
                        String iconUrl = null;
                        if (icon != null && !icon.isEmpty()) {
                                iconUrl = notifi_img_service
                                                .upload_image(notificationRequestForDB.getId(), icon, TypeImage.ICON)
                                                .getNotification_url();
                        }

                        Notification.Builder notificationBuilder = Notification.builder()
                                        .setTitle(notificationRequest.getTitle())
                                        .setBody(notificationRequest.getBody());
                        if (imageUrl != null) {
                                notificationBuilder.setImage(imageUrl);
                        }
                        Message.Builder messageBuilder = Message.builder()
                                        .setNotification(notificationBuilder.build())
                                        .setTopic(normalizedTopic);
                        if (iconUrl != null) {
                                messageBuilder.putData("icon", iconUrl);
                        }
                        return ApiResponse.success(firebaseMessaging.send(messageBuilder.build()));
                } catch (FirebaseMessagingException e) {
                        log.error("Firebase send-to-topic failed. topic={}, errorCode={}, message={}",
                                        normalizedTopic, e.getMessagingErrorCode(), e.getMessage(), e);
                        throw new IllegalArgumentException(buildFirebaseErrorMessage("topic", e));
                }
        }

        // // convenience: promotion broadcast to the "promotions" topic
        // @Override
        // public ApiResponse<String> sendPromotionNotification(NotificationRequest
        // notificationRequest) {
        // return sendNotificationByTopic(notificationRequest, "promotions");
        // }

        private void validateCommonPayload(NotificationRequest notificationRequest) {
                if (notificationRequest == null) {
                        throw new IllegalArgumentException("Notification payload is required");
                }
                if (isBlank(notificationRequest.getTitle()) && isBlank(notificationRequest.getBody())) {
                        throw new IllegalArgumentException("Either title or body must be provided");
                }
        }

        private String requireText(String value, String fieldName) {
                if (isBlank(value)) {
                        throw new IllegalArgumentException(fieldName + " is required");
                }
                return value.trim();
        }

        private String requireTopic(String topic) {
                String normalizedTopic = requireText(topic, "topic");
                if (!normalizedTopic.matches("[a-zA-Z0-9-_.~%]+")) {
                        throw new IllegalArgumentException("topic contains invalid characters");
                }
                return normalizedTopic;
        }

        private String normalizeImage(String url) {
                return isBlank(url) ? null : url;
        }

        private boolean isBlank(String value) {
                return value == null || value.trim().isEmpty();
        }

        private String buildFirebaseErrorMessage(String targetType, FirebaseMessagingException exception) {
                String errorCode = exception.getMessagingErrorCode() != null
                                ? exception.getMessagingErrorCode().name()
                                : "UNKNOWN";
                return "Firebase failed to send " + targetType + " notification (" + errorCode + "): "
                                + exception.getMessage();
        }
}
