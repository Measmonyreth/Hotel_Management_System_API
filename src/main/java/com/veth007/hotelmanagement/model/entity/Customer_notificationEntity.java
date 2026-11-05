package com.veth007.hotelmanagement.model.entity;

import java.time.Instant;

import com.veth007.hotelmanagement.model.entity.Notification.NotificationEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "customer_notification")
public class Customer_notificationEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(targetEntity = NotificationEntity.class)
    @JoinColumn (name = "notification_id")
    private NotificationEntity notification;

    @ManyToOne(targetEntity = CustomerEntity.class)
    @JoinColumn (name = "customer_id", nullable = true)
    private  CustomerEntity customer;
    
    @Column(name = "read_at")
    private Instant readAt;
}
