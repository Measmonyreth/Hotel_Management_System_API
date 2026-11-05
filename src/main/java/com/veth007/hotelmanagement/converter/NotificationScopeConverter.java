package com.veth007.hotelmanagement.converter;
import com.veth007.hotelmanagement.constant.enums.NotificationScope;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationScopeConverter implements AttributeConverter<NotificationScope, String>  {
    public String convertToDatabaseColumn(NotificationScope v) {
        return v == null ? null : v.name().toLowerCase();
    }
    public NotificationScope convertToEntityAttribute(String db) {
        return db == null ? null : NotificationScope.valueOf(db.toUpperCase());
    }
}
