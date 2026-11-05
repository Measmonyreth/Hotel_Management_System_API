package com.veth007.hotelmanagement.model.entity.Room;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.RoomType;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.StatusRoom;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;
import com.veth007.hotelmanagement.model.entity.BookItemEntity;
import com.veth007.hotelmanagement.model.entity.FavoritesEntity;
import com.veth007.hotelmanagement.model.entity.FloorEntity;
import com.veth007.hotelmanagement.model.entity.TaskEnttity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table(name = "rooms")
@Entity(name = "rooms")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class RoomEntity extends BaseSoftDeleted<Long> {

    @Column(nullable = false, unique = true)
    private String roomName;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRoom statusRoom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    // rating
    @Column(name = "rating")
    private Double rating;

    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status action;

    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Column(name = "discount_price")
    private BigDecimal discountPrice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "floor_id", referencedColumnName = "id")
    private FloorEntity floor;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "room", cascade = CascadeType.ALL)
    private List<TaskEnttity> tasks = new ArrayList<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "room", cascade = CascadeType.ALL)
    private List<RoomImageEntity> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "room", cascade = CascadeType.ALL)
    private List<FavoritesEntity> favoritesEntities = new ArrayList<>();

    // @OneToMany(fetch = FetchType.EAGER, mappedBy = "room", cascade =
    // CascadeType.ALL)
    // private List<BookItemEntity> bookItems = new ArrayList<>();
}
