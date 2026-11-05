package com.veth007.hotelmanagement.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.notification.NotificationRequest;
import com.veth007.hotelmanagement.service.Firebase.FirebaseMessagingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class PushNotificationController {
    private final FirebaseMessagingService firebaseMessagingService;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping(value = "/send-token", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> sendToToken(@ModelAttribute NotificationRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        return ResponseEntity.ok(firebaseMessagingService.sendNotificationByToken(request, file, icon));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping(value = "/send-topic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> sendToTopic(@ModelAttribute NotificationRequest request,
            @RequestParam String topic,
             @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "icon", required = false) MultipartFile icon) {
        return ResponseEntity.ok(firebaseMessagingService.sendNotificationByTopic(request, topic, file, icon));
    }

    // @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or
    // hasAuthority('STAFF') )")
    // @PostMapping("/send-promotion")
    // public ResponseEntity<ApiResponse<String>> sendPromotion(@RequestBody
    // NotificationRequest request) {
    // return
    // ResponseEntity.ok(firebaseMessagingService.sendPromotionNotification(request));
    // }
}
