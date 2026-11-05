package com.veth007.hotelmanagement.service.cloudinary;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.veth007.hotelmanagement.model.request.Cloudinary.CloudinaryUploadResult;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload any image to a specific folder
     * 
     * @param file   MultipartFile from request
     * @param folder e.g. "hotel/rooms/5", "hotel/floors", "hotel/avatars"
     * @return CloudinaryUploadResult with url + publicId
     */
    public CloudinaryUploadResult upload(MultipartFile file, String folder) {
        try {
            File tempFile = File.createTempFile("upload_", file.getOriginalFilename());
            file.transferTo(tempFile);

            Map result = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto" // handles image & video
            ));
            
            tempFile.delete();

            return CloudinaryUploadResult.builder()
                    .secureUrl((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary delete failed", e);
        }
    }
}
