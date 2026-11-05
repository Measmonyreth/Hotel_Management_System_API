package com.veth007.hotelmanagement.service.Room.Implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomImageEntity;
import com.veth007.hotelmanagement.model.request.Cloudinary.CloudinaryFolder;
import com.veth007.hotelmanagement.model.request.Cloudinary.CloudinaryUploadResult;
import com.veth007.hotelmanagement.repository.RoomImageRepository;
import com.veth007.hotelmanagement.repository.RoomRepository;
import com.veth007.hotelmanagement.service.cloudinary.CloudinaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomImageService {
    private final RoomImageRepository roomImageRepository;
    private final RoomRepository roomRepository;
    private final CloudinaryService cloudinaryService;

//    private final String FOLDER_PATH = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "room_images" + File.separator;

    @Transactional
    public RoomImageEntity upload_image(Long room_id, MultipartFile file) {
        try {
            RoomEntity room = roomRepository.findById(room_id)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            // // Create directory if not exists
            // String roomFolder = FOLDER_PATH + room_id + File.separator;
            // File dir = new File(roomFolder);
            // if (!dir.exists())
            // dir.mkdirs();

            // // sanitize filename
            // String original = file.getOriginalFilename();
            // String safeName = original != null ? Paths.get(original).getFileName().toString()
            //     : ("upload_" + System.currentTimeMillis());

            // // Public URL to access image via endpoint (fix typo)
            // String imageUrl = "/api/hotel_management/room/" + room_id + "/images/" + safeName;

            // String filePath = roomFolder + safeName;
            // file.transferTo(new File(filePath));

            // RoomImageEntity image = RoomImageEntity.builder()
            //     .fileName(safeName)
            //     .fileType(file.getContentType())
            //     .filePath(filePath)
            //     .imageUrl(imageUrl)
            //     .room(room)
            //     .build();
            // return roomImageRepository.save(image);
            
            CloudinaryUploadResult uploaded = cloudinaryService.upload(file, CloudinaryFolder.room(room_id));
            RoomImageEntity image = RoomImageEntity.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(uploaded.getPublicId())    // reuse filePath column to store public_id
                .imageUrl(uploaded.getSecureUrl())    // direct Cloudinary CDN URL
                .room(room)
                .build();

            return roomImageRepository.save(image);
        } catch (Exception e) {

              e.printStackTrace();
            // TODO: handle exception
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    // public byte[] getRoomImage(Long roomId, String fileName) throws IOException {
    //     String filePath = FOLDER_PATH + roomId + "/" + fileName;
    //     return Files.readAllBytes(new File(filePath).toPath());
    // }
}
