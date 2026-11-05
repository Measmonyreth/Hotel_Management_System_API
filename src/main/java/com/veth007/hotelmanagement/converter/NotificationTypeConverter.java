package com.veth007.hotelmanagement.converter;
import com.veth007.hotelmanagement.constant.enums.NotificationType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String>  {
        public String convertToDatabaseColumn(NotificationType v) {
        return v == null ? null : v.name().toLowerCase();
    }
    public NotificationType convertToEntityAttribute(String db) {
        return db == null ? null : NotificationType.valueOf(db.toUpperCase());
    }
}
