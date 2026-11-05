package com.veth007.hotelmanagement.model.entity.Notification;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.veth007.hotelmanagement.constant.enums.NotificationScope;
import com.veth007.hotelmanagement.constant.enums.NotificationType;
import com.veth007.hotelmanagement.constant.enums.PaymentStatus;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.converter.NotificationScopeConverter;
import com.veth007.hotelmanagement.converter.NotificationTypeConverter;
import com.veth007.hotelmanagement.converter.PaidStatusConverter;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;
import com.veth007.hotelmanagement.model.entity.Customer_notificationEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NotificationEntity extends BaseSoftDeleted<Long> {

    @Convert(converter = NotificationScopeConverter.class)
    @Column(nullable = false, length = 10)
    private NotificationScope scope;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "notification")
    private List<Customer_notificationEntity> customerNotifications;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "notification")
    private List<NotificationImageEntity>  image ;


    @Column(name = "entity_type", length = 30)
    private String entityType; // "booking", "promotion", "room", or null

    @Column(name = "entity_id")
    private Long entityId;

    @Convert(converter = PaidStatusConverter.class)
    // must fit longest PaymentStatus name in lowercase, e.g. "partially_refunded" (18)
    @Column(name = "paid_status", length = 30)
    private PaymentStatus paidStatus; // only for booking_success

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", length = 20)
    private Status notificationStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;



}
