package com.veth007.hotelmanagement.model.request.Cloudinary;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CloudinaryUploadResult {
    private String secureUrl;   // store as imageUrl / avatarUrl / iconUrl
    private String publicId;    // store as filePath / publicId for delete
}
