package com.veth007.hotelmanagement.model.entity.Notification;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

@Table(name = "notification_images")
@Entity(name = "notification_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NotificationImageEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

      @Enumerated(EnumType.STRING)
    @Column(nullable = false) // ✅ needs NOT NULL constraint
    private TypeImage type;

    @Column(nullable = false)
    private String filePath; // path on disk

    @Column(nullable = false)
    private String notification_url; // public URL for response

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", referencedColumnName = "id")
    private NotificationEntity notification;

    public enum TypeImage{
        IMAGE, ICON
    }
}
