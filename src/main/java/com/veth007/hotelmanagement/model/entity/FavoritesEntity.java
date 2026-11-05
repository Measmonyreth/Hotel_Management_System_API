package com.veth007.hotelmanagement.model.entity;

import com.veth007.hotelmanagement.constant.enums.FavoriteStatus;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@Table(name = "favorites")
@NoArgsConstructor
@AllArgsConstructor
public class FavoritesEntity extends BaseSoftDeleted<Long> {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false, referencedColumnName = "id")
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false, referencedColumnName = "id")
    private RoomEntity room;

    @Column(name = "favoriteStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private FavoriteStatus favoriteStatus;
}
