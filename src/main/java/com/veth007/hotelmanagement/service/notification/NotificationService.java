package com.veth007.hotelmanagement.service.notification;

import com.veth007.hotelmanagement.repository.BookRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.monitoring.v3.Service.Custom;
import com.veth007.hotelmanagement.constant.enums.BookStatus;
import com.veth007.hotelmanagement.constant.enums.NotificationScope;
import com.veth007.hotelmanagement.constant.enums.NotificationType;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.BookEntity;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.Customer_notificationEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;
import com.veth007.hotelmanagement.model.request.notification.NotificationRequest;
import com.veth007.hotelmanagement.model.response.Customer_notification.Customer_notificationResponse;
import com.veth007.hotelmanagement.model.response.notification.NotificationListResponse;
import com.veth007.hotelmanagement.model.response.notification.NotificationResponse;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.Customer_notificationRepository;
import com.veth007.hotelmanagement.repository.NotificationRepository;
import com.veth007.hotelmanagement.service.Firebase.FirebaseMessagingService;

import lombok.RequiredArgsConstructor;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

        private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

        private final BookRepository bookRepository;
        private final NotificationRepository repo;
        private final CustomerRepository customerRepo;
        private final Customer_notificationRepository customer_notificationRepository;
        private final FirebaseMessagingService firebaseMessagingService;

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public Customer_notificationEntity bookingSuccess(Long customerId, Long bookingId, String roomNumber,
                        PaymentStatus paid,
                        int amountPaid, int amountDue) {
                return bookingSuccess(customerId, bookingId, roomNumber, paid, amountPaid, amountDue, null);
        }

        // REQUIRES_NEW: never let a notification DB failure abort an outer
        // booking/payment TX
        // (PostgreSQL would otherwise reject all later SQL with "current transaction is
        // aborted")
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public Customer_notificationEntity bookingSuccess(Long customerId, Long bookingId, String roomNumber,
                        PaymentStatus paid,
                        int amountPaid, int amountDue, String recipientToken) {
                if (customerId != null && bookingId != null) {
                        var existing = customer_notificationRepository
                                        .findFirstByNotification_TypeAndNotification_EntityIdAndCustomer_Id(
                                                        NotificationType.BOOKING_SUCCESS, bookingId, customerId);
                        if (existing.isPresent()) {
                                return existing.get();
                        }
                }
                CustomerEntity customer = customerId != null
                                ? customerRepo.findById(customerId).orElse(null)
                                : null;

                if (recipientToken != null && !recipientToken.isBlank()) {
                        try {
                                firebaseMessagingService.sendPushByToken(
                                                NotificationRequest.builder()
                                                                .recipientToken(recipientToken.trim())
                                                                .title("Booking confirmed")
                                                                .body("Your booking is confirmed. Room Number: "
                                                                                + roomNumber)
                                                                .build()
                                        
                                                        );

                        } catch (Exception pushEx) {
                                log.warn("Booking notification push failed for bookingId={}", bookingId, pushEx);
                        }
                }

                NotificationEntity notificationEntity = repo.save(NotificationEntity.builder()
                                .scope(NotificationScope.PERSONAL)
                                .type(NotificationType.BOOKING_SUCCESS)
                                // .customer(customer != null ? customer : null)
                                .title("Booking confirmed")
                                .body("Your booking is confirmed. Room Number: " + roomNumber)
                                .entityType("booking")
                                .entityId(bookingId != null ? bookingId : null)
                                .paidStatus(paid != null ? paid : null)
                                .notificationStatus(Status.ACTIVE)
                                .actionUrl("/bookings/" + bookingId)
                                .data(Map.of("amount_paid", amountPaid, "amount_due", amountDue))
                                .build());

                return customer_notificationRepository.save(
                                Customer_notificationEntity.builder()
                                                .notification(notificationEntity)
                                                .customer(customer != null ? customer : null)
                                                .build());
        }

        public Customer_notificationEntity bookingFailed(Long customerId, Long bookingId, String recipientToken) {
                return bookingNotification(customerId, bookingId, NotificationType.BOOKING_FAILED,
                                "Booking failed", "Your booking could not be completed.", Status.ACTIVE,
                                recipientToken);
        }

        public Customer_notificationEntity bookingCheckin(Long customerId, Long bookingId, String roomNumber,
                        String recipientToken) {
                return bookingNotification(customerId, bookingId, NotificationType.BOOKING_CHECKIN,
                                "Checked in", "You have been checked in Room Number: " + roomNumber, Status.ACTIVE,
                                recipientToken);
        }

        public Customer_notificationEntity bookingCheckout(Long customerId, Long bookingId, String roomNumber,
                        String recipientToken) {
                return bookingNotification(customerId, bookingId, NotificationType.BOOKING_CHECKOUT,
                                "Checked out", "You have been checked out Room Number: " + roomNumber, Status.ACTIVE,
                                recipientToken);
        }

        public Customer_notificationEntity bookingCompleted(Long customerId, Long bookingId, String roomNumber,
                        String recipientToken) {
                return bookingNotification(customerId, bookingId, NotificationType.BOOKING_COMPLETED,
                                "Booking completed", "Your booking is now complete Room Number: " + roomNumber,
                                Status.ACTIVE,
                                recipientToken);
        }

        public NotificationEntity newPromotion(String title, String body) {
                return repo.save(NotificationEntity.builder()
                                .scope(NotificationScope.GENERAL)
                                .type(NotificationType.PROMOTION_NEW) // customer stays null
                                .title(title).body(body)
                                .entityType("promotion") // category label; no entityId (no promotions table)
                                .actionUrl("/promotions") // listing page, not a specific promo
                                .build());
        }

        public NotificationEntity newRoom(Long roomId, String title, String body) {
                return repo.save(NotificationEntity.builder()
                                .scope(NotificationScope.GENERAL)
                                .type(NotificationType.ROOM_NEW) // customer stays null
                                .title(title).body(body)
                                .entityType("room").entityId(roomId)
                                .actionUrl(roomId != null ? "/rooms/" + roomId : "/rooms")
                                .build());
        }

        public NotificationEntity infoGeneral(String title, String body) {
                return repo.save(NotificationEntity.builder()
                                .scope(NotificationScope.GENERAL)
                                .type(NotificationType.INFO_GENERAL) // customer stays null
                                .title(title).body(body)
                                .build());
        }

        // shared builder for the simple PERSONAL booking notifications
        private Customer_notificationEntity bookingNotification(Long customerId, Long bookingId,
                        NotificationType type, String title, String body, Status notificationStatus,
                        String recipientToken) {
                if (customerId != null && bookingId != null) {
                        var existing = customer_notificationRepository
                                        .findFirstByNotification_TypeAndNotification_EntityIdAndCustomer_Id(
                                                        type, bookingId, customerId);
                        if (existing.isPresent()) {
                                return existing.get();
                        }
                }
                CustomerEntity customer = customerId != null
                                ? customerRepo.findById(customerId).orElse(null)
                                : null;
                NotificationEntity notificationEntity = repo.save(NotificationEntity.builder()
                                .scope(NotificationScope.PERSONAL)
                                .type(type)
                                // .customer(customer)
                                .title(title).body(body)
                                .entityType("booking")
                                .entityId(bookingId)
                                .notificationStatus(notificationStatus)
                                .actionUrl("/bookings/" + bookingId)
                                .build());

                Customer_notificationEntity customer_notificationEntity = customer_notificationRepository.save(
                                Customer_notificationEntity.builder()
                                                .notification(notificationEntity)
                                                .customer(customer != null ? customer : null)
                                                .build());
                BookEntity bookEntity = bookRepository.findById(bookingId).get();
                if (bookEntity.getBookStatus() == BookStatus.COMPLETED ||
                                bookEntity.getBookStatus() == BookStatus.CHECKED_IN ||
                                bookEntity.getBookStatus() == BookStatus.CHECKED_OUT) {
                        if (recipientToken != null && !recipientToken.isBlank()) {
                                try {
                                        firebaseMessagingService.sendPushByToken(
                                                        NotificationRequest.builder()
                                                                        .recipientToken(recipientToken.trim())
                                                                        .title(title)
                                                                        .body(body)
                                                                        .build());
                                } catch (Exception pushEx) {
                                        // push is best-effort; keep persisting the in-app notification
                                }
                        }
                }
                return customer_notificationEntity;
        }

        @Transactional(readOnly = true)
        public List<NotificationResponse> personalFeed(Long customerId) {

                List<NotificationEntity> personal = customer_notificationRepository
                                .findNotificationsByCustomerIdAndScope(customerId, NotificationScope.PERSONAL);

                List<NotificationEntity> general = repo.findByScopeOrderByCreatedDateDesc(NotificationScope.GENERAL);

                Stream<NotificationResponse> personalResponses = personal.stream()
                                .map(NotificationResponse::fromEntity);

                Stream<NotificationResponse> generalResponses = general.stream()
                                .map(NotificationResponse::fromEntity);

                return Stream.concat(personalResponses, generalResponses)
                                .sorted(Comparator.comparing(NotificationResponse::getCreatedDate).reversed())
                                .toList();

        }

        public long unreadCount(Long customerId) {
                long noti_gener_for_user = repo.countByScope(NotificationScope.GENERAL);
                long general_read = customer_notificationRepository
                                .countByCustomer_IdAndReadAtIsNotNullAndNotification_Scope(
                                                customerId, NotificationScope.GENERAL);

                long unread_gereral = noti_gener_for_user - general_read;
                long unread_personal = customer_notificationRepository
                                .countByCustomer_IdAndReadAtIsNullAndNotification_Scope(customerId,
                                                NotificationScope.PERSONAL);

                return unread_gereral + unread_personal;
        }

        @Transactional
        public void markRead(Long noti_id, Long customerId) {
                if (customer_notificationRepository.existsByNotification_IdAndCustomer_IdAndReadAtIsNotNull(noti_id,
                                customerId)) {
                        return;
                }
                if (customer_notificationRepository.existsByNotification_IdAndCustomer_Id(noti_id, customerId)) {

                        customer_notificationRepository.updatemarkRead(noti_id, customerId, Instant.now());
                } else {
                        customer_notificationRepository.insertRead(noti_id, customerId, Instant.now());
                }
        }

        @Transactional
        public void markReadAll(Long customerId) {
                List<NotificationEntity> personal = customer_notificationRepository
                                .findNotificationsByCustomerIdAndScope(customerId, NotificationScope.PERSONAL);

                List<NotificationEntity> general = repo.findByScopeOrderByCreatedDateDesc(NotificationScope.GENERAL);

                personal.forEach(n -> markRead(n.getId(), customerId));
                general.forEach(n -> markRead(n.getId(), customerId));
        }

        @Transactional(readOnly = true)
        public NotificationListResponse getall(Integer page, Integer size, boolean isPage) {

                Pageable pageable;
                if (isPage) {
                        pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC,
                                        "createdDate"));
                } else {
                        pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC,
                                        "createdDate"));
                }

                return NotificationListResponse.fromEntity(repo.findAllWithCustomerNotificationAndNotificationImage(pageable).getContent(),
                                repo);
        }

        @Transactional(readOnly = true)
        public NotificationResponse getById(Long id) {
                NotificationEntity notification = repo
                                .findByIdWithCustomerNotificationAndNotificationImage(id)
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + id));
                // Initialize the other lazy List in a separate query within this
                // transaction; fetching both List associations together is unsafe.
                repo.findByIdWithNotificationImages(id);
                return NotificationResponse.fromEntity(notification);
        }

        @Transactional
        public void delete(Long id) {
                NotificationEntity notification = repo.findById(id)
                                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
                if (notification.getDeletedDate() != null) {
                        throw new RuntimeException("Notification already deleted");
                }

                notification.setDeletedBy(SecurityContextHolder.getContext().getAuthentication().getName());
                notification.setDeletedDate(LocalDateTime.now());
                notification.setNotificationStatus(Status.DELETED);
                repo.save(notification);
        }

        // public NotificationResponse update(Long id, NotificationRequest request) {
        //         NotificationEntity notification = repo.findById(id)
        //                         .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        //         notification.setTitle(request.getTitle());
        //         notification.setBody(request.getBody());
        //         notification.setImage();
        // }

}
