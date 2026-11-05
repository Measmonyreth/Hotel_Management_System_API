package com.veth007.hotelmanagement.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.CustomerDetails;
import com.veth007.hotelmanagement.model.entity.Customer_notificationEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;
import com.veth007.hotelmanagement.model.request.notification.NotificationRequest;
import com.veth007.hotelmanagement.model.response.Customer_notification.Customer_notificationResponse;
import com.veth007.hotelmanagement.model.response.notification.NotificationListResponse;
import com.veth007.hotelmanagement.model.response.notification.NotificationResponse;
import com.veth007.hotelmanagement.service.notification.NotificationService;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @PostMapping("/booking-success")
    public ResponseEntity<ApiResponse<Customer_notificationResponse>> bookingSuccess(
            @Valid @RequestBody NotificationRequest request) {
        Customer_notificationEntity notification = this.notificationService.bookingSuccess(
                request.getCustomerId(),
                request.getBookingId(),
                null,
                request.getPaidStatus(),
                request.getAmountPaid(),
                request.getAmountDue(),
                request.getRecipientToken());
        return ResponseEntity.ok(ApiResponse.success(Customer_notificationResponse.fromEntity(notification)));
    }

    // @PostMapping("/notification/promotion")
    // public ResponseEntity<ApiResponse<NotificationResponse>> newPromotion(
    // @RequestBody NotificationRequest request) {
    // NotificationEntity notification = this.notificationService.newPromotion(
    // request.getTitle(),
    // request.getBody());
    // return
    // ResponseEntity.ok(ApiResponse.success(NotificationResponse.fromEntity(notification)));
    // }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @GetMapping("/personal")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> personalFeed(
            @AuthenticationPrincipal CustomerDetails customerDetails) {
        List<NotificationResponse> notifications = this.notificationService.personalFeed(customerDetails.getId());
                
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
     @GetMapping("/unread-count/{customerId}")
    public ResponseEntity<ApiResponse<Long>> unreadCount(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                this.notificationService.unreadCount(customerId)));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @PatchMapping("/{noti_id}/read")
    public ResponseEntity<ApiResponse<String>> markRead(
            @PathVariable Long noti_id,
            @AuthenticationPrincipal CustomerDetails customerDetails) {
        this.notificationService.markRead(noti_id,
                customerDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read"));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or  hasAuthority('CUSTOMER') or hasAuthority('MANAGER'))")
    @PatchMapping("/ ")
    public ResponseEntity<ApiResponse<String>> markReadAll(
            @AuthenticationPrincipal CustomerDetails customerDetails) {
        this.notificationService.markReadAll(customerDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read"));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) boolean isPage) {
        NotificationListResponse notifications = this.notificationService.getall(page, size, isPage);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable Long id) {
        NotificationResponse notification = this.notificationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable String id) {
        this.notificationService.delete(HashIdUtil.resolveId(id));
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted successfully"));
    }
}
