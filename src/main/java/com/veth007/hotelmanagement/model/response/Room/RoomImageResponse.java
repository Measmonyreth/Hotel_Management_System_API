package com.veth007.hotelmanagement.model.response.Room;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.Room.RoomImageEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomImageResponse {
    private Long id;
    private String fileName;
    private String filePath;    // raw disk path
    private String imageUrl;    // accessible HTTP URL

    public static RoomImageResponse fromEntity(RoomImageEntity roomImageEntity) {
        return RoomImageResponse.builder()
                .id(roomImageEntity.getId())
                .fileName(roomImageEntity.getFileName())
                .filePath(roomImageEntity.getFilePath())
                .imageUrl(roomImageEntity.getImageUrl())
                .build();
    }
}
