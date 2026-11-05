package com.veth007.hotelmanagement.model.request.notification;

import java.util.Map;

import lombok.Data;

@Data
public class NotificationMessage {
    private String recipientToken;
    private String title;
    private String body;
    private String image;
    private Map<String, String> data;
}
