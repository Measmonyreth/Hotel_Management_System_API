package com.veth007.hotelmanagement.model.entity;

import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "customers")
@Entity(name = "customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class CustomerEntity extends BaseSoftDeleted<Long> {

    @Column(name = "customer_id", unique = true, nullable = false)
    private String customerId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    private String picture; // ✅ plain column — Google profile picture

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // ✅ needs NOT NULL constraint
    private AuthProvider provider;

    private String providerId; // ✅ plain column — Google sub ID
    @Column(name = "phone", unique = true)
    private String phone;

    @Column(nullable = false)
    private boolean emailVerified; // ✅ default to false, updated by verification process

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "city_or_province")
    private String city_or_province;

    @Column(name = "country")
    private String country;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Customer_notificationEntity> customer_notification;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<FcmTokenEntity> fcmTokenEntities;

    @OneToOne(mappedBy = "customer", cascade = jakarta.persistence.CascadeType.ALL, fetch = jakarta.persistence.FetchType.LAZY)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private VipCustomerEntity vipCustomer;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private List<FavoritesEntity> favoritesEntities;

    public enum AuthProvider {
        LOCAL, GOOGLE
    }
}
