package com.veth007.hotelmanagement.service.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationImageEntity;
import com.veth007.hotelmanagement.model.entity.Notification.NotificationImageEntity.TypeImage;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomImageEntity;
import com.veth007.hotelmanagement.model.request.Cloudinary.CloudinaryFolder;
import com.veth007.hotelmanagement.model.request.Cloudinary.CloudinaryUploadResult;
import com.veth007.hotelmanagement.repository.NotificationImageRepository;
import com.veth007.hotelmanagement.repository.NotificationRepository;
import com.veth007.hotelmanagement.service.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class NotificationImageService {
    private final NotificationImageRepository notificationImageRepository;
    private final NotificationRepository notificationRepository;
        private final CloudinaryService cloudinaryService;

    @Transactional 
    public NotificationImageEntity upload_image(Long noti_id, MultipartFile file, TypeImage typeImg ) {
        try {
            NotificationEntity notification = notificationRepository.findById(noti_id)
                    .orElseThrow(() -> new RuntimeException("notification not found"));
                 
            CloudinaryUploadResult uploaded = cloudinaryService.upload(file, CloudinaryFolder.notification(noti_id));
            NotificationImageEntity notificationImg = NotificationImageEntity.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(uploaded.getPublicId())    // reuse filePath column to store public_id
                .notification_url(uploaded.getSecureUrl())    // direct Cloudinary CDN URL
                .notification(notification)
                .type(typeImg)
                .build();

            return notificationImageRepository.save(notificationImg);
        } catch (Exception e) {

              e.printStackTrace();
            // TODO: handle exception
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }
}
