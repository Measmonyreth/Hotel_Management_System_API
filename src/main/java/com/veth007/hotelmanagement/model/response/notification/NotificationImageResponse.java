package com.veth007.hotelmanagement.model.response.notification;
import java.io.Serializable;

import com.veth007.hotelmanagement.model.entity.Notification.NotificationImageEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomImageEntity;
import com.veth007.hotelmanagement.model.response.Room.RoomImageResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationImageResponse implements Serializable {
        private Long id;
    private String fileName;
    private String fileType;
    private String filePath;    // raw disk path
    private String type;
    private String notification_url;    // accessible HTTP URL

    public static NotificationImageResponse fromEntity(NotificationImageEntity notificationImageEntity) {
        return NotificationImageResponse.builder()
                .id(notificationImageEntity.getId())
                .fileName(notificationImageEntity.getFileName())
                .fileType(notificationImageEntity.getFileType())
                .type(notificationImageEntity.getType().toString())
                .filePath(notificationImageEntity.getFilePath())
                .notification_url(notificationImageEntity.getNotification_url())
                .build();
    }
}
