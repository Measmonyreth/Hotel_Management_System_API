package com.veth007.hotelmanagement.converter;

import com.veth007.hotelmanagement.constant.enums.PaymentStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PaidStatusConverter implements AttributeConverter<PaymentStatus, String>  {
     public String convertToDatabaseColumn(PaymentStatus v) {
        return v == null ? null : v.name().toLowerCase();
    }
    public PaymentStatus convertToEntityAttribute(String db) {
        return db == null ? null : PaymentStatus.valueOf(db.toUpperCase());
    }
}
