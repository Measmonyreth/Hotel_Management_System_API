package com.veth007.hotelmanagement.model.request.Cloudinary;
public final class CloudinaryFolder {
    private CloudinaryFolder() {}

    public static final String ROOM   = "hotel/rooms";
    public static final String FLOOR  = "hotel/floors";
    public static final String AVATAR = "hotel/avatars";
    public static final String NOTIFICATION = "hotel/notifications";

    // room images go under hotel/rooms/{roomId}
    public static String room(Long roomId) {
        return ROOM + "/" + roomId;
    }

    public static String notification (Long notificationId) {
        return NOTIFICATION + "/" + notificationId;
    }
}
